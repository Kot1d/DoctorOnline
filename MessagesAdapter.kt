package com.doctoronline.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.doctoronline.databinding.ItemMessageRecipientBinding
import com.doctoronline.databinding.ItemMessageSenderBinding
import com.doctoronline.dto.MessageDto
import java.text.SimpleDateFormat

class MessagesAdapter() :
  RecyclerView.Adapter<ViewHolder>() {
  private var messagesList = listOf<MessageDto>()

  class SenderHolder(val binding: ItemMessageSenderBinding) : ViewHolder(binding.root)
  class RecipientHolder(val binding: ItemMessageRecipientBinding) : ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return when (viewType) {
      1 -> SenderHolder(
        ItemMessageSenderBinding.inflate(
          LayoutInflater.from(parent.context),
          parent,
          false
        )
      )

      else -> RecipientHolder(
        ItemMessageRecipientBinding.inflate(
          LayoutInflater.from(parent.context),
          parent,
          false
        )
      )
    }
  }

  override fun getItemViewType(position: Int): Int {
    return if (messagesList[position].userIsSender) 1 else 2
  }

  override fun getItemCount(): Int {
    return messagesList.size
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
    val item = messagesList[position]
    if (item.userIsSender) {
      val holder = viewHolder as SenderHolder
      with(holder.binding) {
        itemMessage.text = item.message
        itemTime.text = SimpleDateFormat("dd.MM | HH:mm").format(item.timestamp)
      }
    } else {
      val holder = viewHolder as RecipientHolder
      with(holder.binding) {
        itemMessage.text = item.message
        itemTime.text = SimpleDateFormat("dd.MM | HH:mm").format(item.timestamp)
      }
    }

  }

  @SuppressLint("NotifyDataSetChanged")
  fun updateList(list: List<MessageDto>) {
    messagesList = list
    notifyDataSetChanged()
  }

}