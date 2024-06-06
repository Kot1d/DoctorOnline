package com.doctoronline

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.doctoronline.dto.NoteDto
import com.doctoronline.dto.StaffDto
import com.doctoronline.utils.AppValueEventListener
import com.doctoronline.utils.Constant
import com.doctoronline.viewmodel.AppViewModel
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
  val appViewModel by viewModels<AppViewModel>()
  private val firebaseDatabase by lazy { FirebaseDatabase.getInstance().reference }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initialize()
  }

  private fun initialize() {
    getStaff()
    getNotes()
  }

  private fun getNotes() {
    firebaseDatabase.child(Constant.NOTES).addValueEventListener(AppValueEventListener {
      val tempList = mutableListOf<NoteDto>()
      for (item in it.children) {
        val id = item.key.toString().trim()
        val staffId = item.child(Constant.STAFF).value.toString().trim()
        val timestamp = try {
          item.child(Constant.TIMESTAMP).value.toString().trim().toLong()
        } catch (_: Exception) {
          System.currentTimeMillis()
        }
        val user = item.child(Constant.USER).value.toString().trim()
        val newItem = NoteDto(id = id, staffId = staffId, timestamp = timestamp, user = user)
        tempList.add(newItem)
      }
      appViewModel.setNotesList(tempList)
    })
  }

  private fun getStaff() {
    firebaseDatabase.child(Constant.STAFF).addValueEventListener(AppValueEventListener {
      val tempList = mutableListOf<StaffDto>()
      for (item in it.children) {
        val id = item.key.toString().trim()
        val title = item.child(Constant.TITLE).value.toString().trim()
        val fullname = item.child(Constant.FULLNAME).value.toString().trim()
        val newItem = StaffDto(id = id, title = title, fullname = fullname)
        tempList.add(newItem)
      }
      appViewModel.setStaffList(tempList)
    })
  }
}