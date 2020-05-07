package com.example.galleryapplication.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.showToast

class ForgetPasswordViewModel(val context: Application) : AndroidViewModel(context)  {
    private val repository = Repository()
    private var emailError = MutableLiveData<String>()
    private var loginState = MutableLiveData<FirebaseViewModel.LoginState>()

    fun getEmailError(): LiveData<String> {
        return emailError
    }

    fun getLoginState(): LiveData<FirebaseViewModel.LoginState> {
        return loginState
    }

    fun passwordReset(email: String) {
        if (TextUtils.isEmpty(email)) {
            emailError.value = "Enter your registered Email Id"

        } else {
            loginState.value = FirebaseViewModel.LoginState.SHOW_PROGRESS
            repository.passwordReset(email).addOnSuccessListener {
                context.showToast("We have sent you instructions to reset your password!")
                loginState.value = FirebaseViewModel.LoginState.HIDE_PROGRESS
            }
                .addOnFailureListener {
                    context.showToast("Failed")
                    loginState.value = FirebaseViewModel.LoginState.HIDE_PROGRESS


                }
        }
    }


}