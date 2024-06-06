package com.doctoronline.dto

data class NoteDto(
  val id: String,
  val staffId: String,
  val timestamp: Long,
  val user: String,
)
