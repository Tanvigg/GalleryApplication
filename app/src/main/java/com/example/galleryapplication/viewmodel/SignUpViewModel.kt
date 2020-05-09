package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.*
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.model.isNetworkAvailable

class SignUpViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var emailError = MutableLiveData<String>()
    private var passwordError = MutableLiveData<String>()
    private var nameError = MutableLiveData<String>()
    private var errMessage = MutableLiveData<String>()

    private val signupStatus = MediatorLiveData<SignupStatus>()
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

    fun getSignUpStatus(): LiveData<SignupStatus> {
        return signupStatus
    }

    fun getError(): LiveData<String> {
        return errMessage
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

    fun signUp(name: String, email: String, password: String, userImage: Uri?) {
        if (!(context.isNetworkAvailable())) {
            errMessage.value = "Network not available"
        } else if (!validateName(name) || !validateEmail(email) || !validatePassword(password)) {
            return
        } else {
            signupStatus.value = SignupStatus.SHOW_PROGRESS
            signupStatus.addSource(repository.signUp(name, email, password, userImage), Observer {
                it.onSuccess {
                    signupStatus.value = SignupStatus.GO_TO_HOMEPAGE
                    signupStatus.value = SignupStatus.HIDE_PROGRESS
                }
                it.onFailure {
                    errMessage.value = it.toString()
                    signupStatus.value = SignupStatus.HIDE_PROGRESS


                }
            })
        }
    }

    enum class SignupStatus {
        SHOW_PROGRESS,
        HIDE_PROGRESS,
        GO_TO_HOMEPAGE,

    }

}


