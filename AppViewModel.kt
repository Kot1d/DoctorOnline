package com.doctoronline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doctoronline.dto.MessageDto
import com.doctoronline.dto.NoteDto
import com.doctoronline.dto.StaffDto

class AppViewModel : ViewModel() {
  private val _staffList = MutableLiveData(listOf<StaffDto>())
  val staffList: LiveData<List<StaffDto>> = _staffList

  fun setStaffList(value: List<StaffDto>) {
    _staffList.value = value
  }

  private val _notesList = MutableLiveData(listOf<NoteDto>())
  val notesList: LiveData<List<NoteDto>> = _notesList

  fun setNotesList(value: List<NoteDto>) {
    _notesList.value = value
  }

  private val _messages = MutableLiveData(listOf<MessageDto>())
  val messages: LiveData<List<MessageDto>> = _messages

  fun addMessage(newMessage: MessageDto) {
    messages.value?.let {
      val tempList = mutableListOf<MessageDto>()
      tempList.addAll(it)
      tempList.add(newMessage)
      _messages.value = tempList
    } ?: run {
      _messages.value = listOf(newMessage)
    }
  }

  fun clearMessages() {
    _messages.value = listOf()
  }
}