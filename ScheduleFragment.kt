package com.doctoronline.presentation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctoronline.MainActivity
import com.doctoronline.R
import com.doctoronline.adapter.StaffAdapter
import com.doctoronline.adapter.TimesAdapter
import com.doctoronline.databinding.FragmentScheduleBinding
import com.doctoronline.dto.NoteDto
import com.doctoronline.dto.StaffDto
import com.doctoronline.utils.Constant
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class ScheduleFragment : Fragment() {
  private val binding by lazy { FragmentScheduleBinding.inflate(layoutInflater) }
  private val appViewModel by lazy { (requireActivity() as MainActivity).appViewModel }
  private val firebaseDatabase by lazy { FirebaseDatabase.getInstance().reference }
  private val userEmail by lazy {
    FirebaseAuth.getInstance().currentUser?.email?.replace("@", "_")?.replace(".", "-") ?: "error"
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = binding.root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initialize()
  }

  private fun initialize() {
    initSchedule()
    initListeners()
  }

  private fun initSchedule() {
    appViewModel.staffList.observe(viewLifecycleOwner) { value ->
      if (value.isNotEmpty()) {
        binding.calendar.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
      } else {
        binding.calendar.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
      }
    }
    appViewModel.notesList.observe(viewLifecycleOwner) { value ->
      setCalendarDays(value)
      observerNonActualNotes(value)
    }
  }

  private fun observerNonActualNotes(value: List<NoteDto>) {
    value.map {
      if (getStartDay(it.timestamp) < getStartDay(System.currentTimeMillis())) {
        firebaseDatabase.child(Constant.NOTES).child(it.id).removeValue()
      }
    }
  }

  private fun setCalendarDays(notesList: List<NoteDto>) {
    binding.calendar.removeDecorators()
    val eventDates = notesList.filter { it.user == userEmail }.map { it.timestamp }
    binding.calendar.addDecorator(object : DayViewDecorator {
      override fun shouldDecorate(day: CalendarDay?): Boolean {
        return eventDates.any { isDateInDay(it, day?.date) }
      }

      override fun decorate(view: DayViewFacade?) {
        val selectionDrawable =
          ResourcesCompat.getDrawable(resources, R.drawable.date_selected, null)
        selectionDrawable?.let { view?.setSelectionDrawable(it) }
      }

    })
  }

  private fun initListeners() {
    binding.btnSupport.setOnClickListener {
      findNavController().navigate(R.id.supportFragment)
    }
    binding.calendar.setOnDateChangedListener { _, date, _ ->
      val currentDay = getStartDay(System.currentTimeMillis())
      val selectedDay = getStartDay(date.date.time)
      if (selectedDay < currentDay) {
        Toast.makeText(requireContext(), "Запись на эту дату невозможна", Toast.LENGTH_SHORT).show()
      } else {
        showBottomStaff(selectedDay)
      }
    }
  }

  private fun showBottomStaff(timestamp: Long) {
    val staffList = appViewModel.staffList.value ?: listOf()
    val bottomDialog = BottomSheetDialog(requireContext())
    val bottomView = layoutInflater.inflate(R.layout.bottom_staff, null)
    bottomDialog.apply {
      setContentView(bottomView)
      show()
    }
    val staffAdapter = StaffAdapter(staffList, selectItem = { item ->
      bottomDialog.dismiss()
      showBottomDates(timestamp, item)
    })
    bottomView.findViewById<TextView>(R.id.bottom_title).text =
      SimpleDateFormat("d MMMM, EEEE").format(timestamp)
    val bottomRv = bottomView.findViewById<RecyclerView>(R.id.bottom_rv)
    bottomRv.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = staffAdapter
    }
  }

  private fun showBottomDates(timestamp: Long, staffDto: StaffDto) {
    val bottomDialog = BottomSheetDialog(requireContext())
    val bottomView = layoutInflater.inflate(R.layout.bottom_dates, null)
    bottomDialog.apply {
      setContentView(bottomView)
      show()
    }
    val allTimeList = generateTimeIntervals()
    val notesList = appViewModel.notesList.value ?: listOf()
    val filteredNotes = notesList.filter {
      it.staffId == staffDto.id && getStartDay(timestamp) == getStartDay(it.timestamp)
    }
    filteredNotes.map { note ->
      val hourMinute = SimpleDateFormat("HH:mm").format(getDayTime(note.timestamp))
      if (allTimeList.contains(hourMinute))
        allTimeList.remove(hourMinute)
    }
    val timesAdapter = TimesAdapter(allTimeList, selectItem = { item ->
      val selectedTimestamp = SimpleDateFormat("HH:mm").parse(item).time
      bottomDialog.dismiss()
      showBottomSign(timestamp + selectedTimestamp + 10800000, staffDto)
    })
    bottomView.findViewById<TextView>(R.id.bottom_title).text =
      SimpleDateFormat("d MMMM, EEEE").format(timestamp)
    val bottomRv = bottomView.findViewById<RecyclerView>(R.id.bottom_rv)
    bottomRv.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = timesAdapter
    }
  }

  private fun showBottomSign(timestamp: Long, staffDto: StaffDto) {
    val bottomDialog = BottomSheetDialog(requireContext())
    val bottomView = layoutInflater.inflate(R.layout.bottom_sign, null)
    bottomDialog.apply {
      setContentView(bottomView)
      setCancelable(false)
      show()
    }
    bottomView.findViewById<TextView>(R.id.bottom_title).text =
      SimpleDateFormat("d MMMM, EEEE, HH:mm").format(timestamp)
    bottomView.findViewById<TextView>(R.id.bottom_staff).text =
      "${staffDto.title}\n${staffDto.fullname}"
    bottomView.findViewById<Button>(R.id.bottom_yes).setOnClickListener { btn ->
      btn.isEnabled = false
      firebaseDatabase.child(Constant.NOTES).let { reference ->
        val id = reference.push().key.toString()
        reference.child(id).child(Constant.STAFF).setValue(staffDto.id)
        reference.child(id).child(Constant.USER).setValue(userEmail)
        reference.child(id).child(Constant.TIMESTAMP).setValue(timestamp).addOnSuccessListener {
          bottomDialog.dismiss()
          showDialogSigned(timestamp)
        }.addOnFailureListener {
          btn.isEnabled = true
          Toast.makeText(requireContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
      }
    }
    bottomView.findViewById<Button>(R.id.bottom_no).setOnClickListener {
      bottomDialog.dismiss()
    }
  }

  private fun showDialogSigned(timestamp: Long) {
    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_signed, null)
    val dialog = AlertDialog.Builder(requireContext())
      .setView(dialogView)
      .create()
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
    dialogView.findViewById<TextView>(R.id.dialog_date).text =
      SimpleDateFormat("dd.MM.yyyy, HH:mm, EEEE").format(timestamp)
    dialogView.findViewById<ImageView>(R.id.dialog_close).setOnClickListener {
      dialog.dismiss()
    }
  }

  private fun isDateInDay(timestamp: Long, date: Date?): Boolean {
    date ?: return false
    val calendar = Calendar.getInstance()
    calendar.time = date
    val startOfDay = calendar.clone() as Calendar
    startOfDay.set(Calendar.HOUR_OF_DAY, 0)
    startOfDay.set(Calendar.MINUTE, 0)
    startOfDay.set(Calendar.SECOND, 0)
    startOfDay.set(Calendar.MILLISECOND, 0)
    val endOfDay = calendar.clone() as Calendar
    endOfDay.set(Calendar.HOUR_OF_DAY, 23)
    endOfDay.set(Calendar.MINUTE, 59)
    endOfDay.set(Calendar.SECOND, 59)
    endOfDay.set(Calendar.MILLISECOND, 999)
    return timestamp in startOfDay.timeInMillis..endOfDay.timeInMillis
  }

  private fun generateTimeIntervals(): MutableList<String> {
    val timeFormat = SimpleDateFormat("HH:mm")
    val times = mutableListOf<String>()
    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 8)
      set(Calendar.MINUTE, 0)
    }
    val endCalendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 17)
      set(Calendar.MINUTE, 0)
    }
    while (calendar.before(endCalendar)) {
      times.add(timeFormat.format(calendar.time))
      calendar.add(Calendar.MINUTE, 15)
    }
    return times
  }

  private fun getStartDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
  }

  private fun getDayTime(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.YEAR, 0)
    calendar.set(Calendar.MONTH, 0)
    calendar.set(Calendar.DAY_OF_WEEK, 0)
    return calendar.timeInMillis
  }
}