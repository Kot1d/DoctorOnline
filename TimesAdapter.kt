package com.doctoronline.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doctoronline.databinding.ItemTimeBinding

class TimesAdapter(
  private val adapterList: List<String>,
  private val selectItem: (String) -> Unit
) : RecyclerView.Adapter<TimesAdapter.ItemHolder>() {

  class ItemHolder(val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
    return ItemHolder(ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun getItemCount(): Int {
    return adapterList.size
  }

  override fun onBindViewHolder(holder: ItemHolder, position: Int) {
    adapterList[position].let { item ->
      with(holder.binding) {
        itemTime.text = item
        root.setOnClickListener {
          selectItem(item)
        }
      }
    }
  }
}