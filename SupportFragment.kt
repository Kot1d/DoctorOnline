package com.doctoronline.presentation

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.doctoronline.MainActivity
import com.doctoronline.R
import com.doctoronline.databinding.FragmentSupportBinding
import com.doctoronline.dto.MessageDto
import com.doctoronline.adapter.MessagesAdapter
import com.doctoronline.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SupportFragment : Fragment() {
  private val binding by lazy { FragmentSupportBinding.inflate(layoutInflater) }
  private val userEmail by lazy {
    FirebaseAuth.getInstance().currentUser?.email?.replace("@", "_")?.replace(".", "-") ?: "error"
  }
  private val database by lazy { FirebaseDatabase.getInstance().reference }
  private val messagesAdapter by lazy { MessagesAdapter() }
  private val valueEventListener by lazy {
    object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        appViewModel.clearMessages()
        for (messageSnapshot in snapshot.children) {
          val sender = messageSnapshot.child(Constant.SENDER).value.toString()
          val message = messageSnapshot.child(Constant.MESSAGE).value.toString()
          val timestamp = try {
            messageSnapshot.child(Constant.TIMESTAMP).value.toString().toLong()
          } catch (_: Exception) {
            System.currentTimeMillis()
          }
          val newMessage =
            MessageDto(
              message = message,
              timestamp = timestamp,
              sender = sender,
              userIsSender = sender != Constant.SUPPORT
            )
          appViewModel.addMessage(newMessage)
        }
      }

      override fun onCancelled(error: DatabaseError) {
      }
    }
  }
  private val appViewModel by lazy { (requireActivity() as MainActivity).appViewModel }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = binding.root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initialize()
  }

  private fun initialize() {
    initViewModel()
    initRecyclerView()
    initListeners()
  }

  private fun initViewModel() {
    appViewModel.messages.observe(viewLifecycleOwner) { list ->
      messagesAdapter.updateList(list)
      binding.itemsRv.smoothScrollToPosition(list.size)
    }
  }

  private fun initRecyclerView() {
    binding.itemsRv.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = messagesAdapter
    }
    database.child(Constant.CHATS).child(userEmail).addValueEventListener(valueEventListener)
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun initListeners() {
    binding.btnSend.isEnabled = false
    binding.btnBack.setOnClickListener {
      findNavController().navigateUp()
    }
    binding.btnSend.setOnClickListener {
      sendMessage()
    }
    binding.inputMessage.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
      }

      override fun afterTextChanged(s: Editable?) {
        val message = binding.inputMessage.text.toString().trim()
        if (message.isNotEmpty()) {
          binding.btnSend.isEnabled = true
          binding.btnSend.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
              requireContext(),
              R.color.red
            )
          )
        } else {
          binding.btnSend.isEnabled = false
          binding.btnSend.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
              requireContext(),
              R.color.gray
            )
          )
        }
      }
    })
  }

  private fun sendMessage() {
    val message = binding.inputMessage.text.toString().trim()
    binding.inputMessage.text.clear()
    val messageReference = database.child(Constant.CHATS).child(userEmail).push()
    messageReference.child(Constant.MESSAGE).setValue(message)
    messageReference.child(Constant.SENDER).setValue(userEmail)
    messageReference.child(Constant.TIMESTAMP).setValue(System.currentTimeMillis())
  }

  override fun onDestroy() {
    super.onDestroy()
    appViewModel.clearMessages()
  }
}