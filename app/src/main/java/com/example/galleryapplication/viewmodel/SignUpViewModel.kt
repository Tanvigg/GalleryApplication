package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository

class SignUpViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var emailError = MutableLiveData<String>()
    private var passwordError = MutableLiveData<String>()
    private var nameError = MutableLiveData<String>()
    private val patternEmail = "^[a-zA-Z0-9_!#\$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$"
    private val patternPassword = "^(?=.*\\d).{6,16}\$"
    //(?=.*d)         : This matches the presence of at least one digit i.e. 0-9.
    //{6,16}          : This limits the length of password from minimum 6 letters to maximum 16 letters.


    fun getNameError(): LiveData<String> {
        return nameError
    }

    fun getEmailError(): LiveData<String> {
        return emailError
    }

    fun getPasswordError(): LiveData<String> {
        return passwordError
    }

    private fun validateEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            emailError.value = "Email can't be blank"
            return false
        } else if (!email.matches(patternEmail.toRegex())) {
            emailError.value = "Invalid email Address"
            return false
        } else {
            emailError.value = null
            return true
        }
    }

    private fun validatePassword(password: String): Boolean {
        if (TextUtils.isEmpty(password)) {
            passwordError.value = "Please Enter Password"
            return false
        } else if (!password.matches(patternPassword.toRegex())) {
            passwordError.value = "Invalid Password"
            return false
        } else {
            passwordError.value = null
            return true
        }
    }

    private fun validateName(name: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            nameError.value = "Please Enter Name"
            return false
        } else {
            nameError.value = null
            return true
        }
    }
    fun signUp(name: String, email: String, password: String, userImage: Uri?): Boolean {
        if (!validateName(name) || !validateEmail(email) || !validatePassword(password)) {
            return false
        } else {
            return repository.signUp(name, email, password, userImage)
        }
    }

}


