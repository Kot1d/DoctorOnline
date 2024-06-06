package com.doctoronline.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AppValueEventListener(val snapshot: (DataSnapshot) -> Unit) : ValueEventListener {
  override fun onDataChange(snapshot: DataSnapshot) {
    snapshot(snapshot)
  }

  override fun onCancelled(error: DatabaseError) {
  }
}