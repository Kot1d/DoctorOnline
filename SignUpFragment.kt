package com.doctoronline.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.doctoronline.R
import com.doctoronline.databinding.FragmentSignUpBinding
import com.doctoronline.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {
  private val binding by lazy { FragmentSignUpBinding.inflate(layoutInflater) }
  private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
  private val firebaseDatabase by lazy { FirebaseDatabase.getInstance().reference }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = binding.root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initialize()
  }

  private fun initialize() {
    initListeners()
  }

  private fun initListeners() {
    binding.btnAuth.setOnClickListener {
      tryAuth()
    }
    binding.btnSign.setOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun tryAuth() {
    val fullname = binding.inputFullname.text.toString().trim()
    val phone = binding.inputPhone.text.toString().trim()
    val address = binding.inputAddress.text.toString().trim()
    val email = binding.inputEmail.text.toString().trim()
    val password = binding.inputPassword.text.toString().trim()
    if (email.isNotEmpty() && password.isNotEmpty() && fullname.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty()) {
      binding.btnAuth.isEnabled = false
      binding.btnSign.isEnabled = false
      firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
        val userEmail =
          firebaseAuth.currentUser?.email?.replace("@", "_")?.replace(".", "-") ?: "error"
        firebaseDatabase.child(Constant.USERS).child(userEmail).let { reference ->
          reference.child(Constant.FULLNAME).setValue(fullname)
          reference.child(Constant.ADDRESS).setValue(address)
          reference.child(Constant.PHONE).setValue(phone).addOnSuccessListener {
            handleAuth()
          }.addOnFailureListener {
            binding.btnAuth.isEnabled = true
            binding.btnSign.isEnabled = true
            Toast.makeText(requireContext(), "Ошибка при создании аккаунта", Toast.LENGTH_SHORT)
              .show()
          }
        }
      }.addOnFailureListener {
        binding.btnAuth.isEnabled = true
        binding.btnSign.isEnabled = true
        Toast.makeText(requireContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(requireContext(), "Заполните поля", Toast.LENGTH_SHORT).show()
    }
  }

  private fun handleAuth() {
    findNavController().popBackStack()
    findNavController().popBackStack()
    findNavController().navigate(R.id.scheduleFragment)
  }
}