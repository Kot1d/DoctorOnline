package com.doctoronline.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doctoronline.databinding.ItemStaffBinding
import com.doctoronline.dto.StaffDto

class StaffAdapter(
  private val adapterList: List<StaffDto>,
  private val selectItem: (StaffDto) -> Unit
) : RecyclerView.Adapter<StaffAdapter.ItemHolder>() {

  class ItemHolder(val binding: ItemStaffBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
    return ItemHolder(ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun getItemCount(): Int {
    return adapterList.size
  }

  override fun onBindViewHolder(holder: ItemHolder, position: Int) {
    adapterList[position].let { item ->
      with(holder.binding) {
        itemPost.text = item.title
        itemFullname.text = item.fullname
        root.setOnClickListener {
          selectItem(item)
        }
      }
    }
  }
}