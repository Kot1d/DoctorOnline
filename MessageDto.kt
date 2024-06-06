package com.doctoronline.dto

data class MessageDto(
  val message: String,
  val timestamp: Long,
  val sender: String,
  val userIsSender: Boolean
)
