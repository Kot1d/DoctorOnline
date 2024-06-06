package com.doctoronline.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.doctoronline.R
import com.doctoronline.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthFragment : Fragment() {
  private val binding by lazy { FragmentAuthBinding.inflate(layoutInflater) }
  private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = binding.root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initialize()
  }

  private fun initialize() {
    checkAuth()
    initListeners()
  }

  private fun initListeners() {
    binding.btnAuth.setOnClickListener {
      tryAuth()
    }
    binding.btnSign.setOnClickListener {
      findNavController().navigate(R.id.signUpFragment)
    }
  }

  private fun tryAuth() {
    val email = binding.inputEmail.text.toString().trim()
    val password = binding.inputPassword.text.toString().trim()
    if (email.isNotEmpty() && password.isNotEmpty()) {
      binding.btnAuth.isEnabled = false
      binding.btnSign.isEnabled = false
      firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
        handleAuth()
      }.addOnFailureListener {
        binding.btnAuth.isEnabled = true
        binding.btnSign.isEnabled = true
        Toast.makeText(requireContext(), "Аккаунт не найден", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(requireContext(), "Заполните поля", Toast.LENGTH_SHORT).show()
    }
  }

  private fun checkAuth() {
    if (firebaseAuth.currentUser != null) {
      handleAuth()
    }
  }

  private fun handleAuth() {
    findNavController().popBackStack()
    findNavController().navigate(R.id.scheduleFragment)
  }
}