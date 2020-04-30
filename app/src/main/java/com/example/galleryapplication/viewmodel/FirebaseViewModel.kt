package com.example.galleryapplication.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Contacts.People.setPhotoData
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.sql.Time

class FirebaseViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var emailError = MutableLiveData<String>()
    private var passwordError = MutableLiveData<String>()
    private var nameError = MutableLiveData<String>()
    private var loginState = MutableLiveData<LoginState>()
    lateinit var account: GoogleSignInAccount
    private val patternEmail = "^[a-zA-Z0-9_!#\$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$"
    private val patternPassword = "^(?=.*\\d).{6,16}\$"
    //(?=.*d)         : This matches the presence of at least one digit i.e. 0-9.
    //{6,16}          : This limits the length of password from minimum 6 letters to maximum 16 letters.
    private lateinit var categoryList: MutableList<Category>
    private  lateinit  var photosList: MutableList<Photos>
    private var savedUserCategories: MutableLiveData<List<Category>> = MutableLiveData()
    private var savedUserPhotos: MutableLiveData<List<Photos>> = MutableLiveData()
    private var savedTimeLinePhotos: MutableLiveData<List<TimeLineModel>> = MutableLiveData()
    private lateinit var tList:List<TimeLineModel>



    fun getEmailError(): LiveData<String> {
        return emailError
    }

    fun getPasswordError(): LiveData<String> {
        return passwordError
    }

    fun getNameError(): LiveData<String> {
        return nameError
    }

    fun getLoginState(): LiveData<LoginState> {
        return loginState
    }


    fun login(email: String, password: String) {
        if (TextUtils.isEmpty(email)) {
            emailError.value = "Email can't be blank"
        } else if (TextUtils.isEmpty(password)) {
            passwordError.value = "Please enter Password"

        } else {
            loginState.value = LoginState.SHOW_PROGRESS
            //authenticate user
            repository.login(email, password).addOnSuccessListener {
                context.showToast("Welcome")
                loginState.value = LoginState.GO_TO_HOMEPAGE
            }

                .addOnFailureListener {
                    //Log.d(TAG, "signInWithEmail:Failed")
                    context.showToast("Failed")
                    loginState.value = LoginState.HIDE_PROGRESS
                }
        }

    }


    enum class LoginState {
        SHOW_PROGRESS,
        HIDE_PROGRESS,
        GO_TO_HOMEPAGE,
        PASS_DATA_AND_GOTO_HOMEPAGE
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


    fun signUp(name: String, email: String, password: String, userImage: Uri): Boolean {
        if (!validateName(name) || !validateEmail(email) || !validatePassword(password)) {
            return false
        } else {
            return repository.signUp(name, email, password, userImage)
        }
    }

    fun passwordReset(email: String) {
        if (TextUtils.isEmpty(email)) {
            emailError.value = "Enter your registered Email Id"

        } else {
            loginState.value = LoginState.SHOW_PROGRESS
            repository.passwordReset(email).addOnSuccessListener {
                context.showToast("We have sent you instructions to reset your password!")
                loginState.value = LoginState.HIDE_PROGRESS
            }
                .addOnFailureListener {
                    context.showToast("Failed")
                    loginState.value = LoginState.HIDE_PROGRESS


                }
        }
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult? =
                Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result!!.isSuccess) {
                account = result.signInAccount!!
                firebaseAuthWithGoogle(account)
            } else {
                context.showToast("Google Sign in Failed")

            }
        }

    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val authCredential: AuthCredential =
            GoogleAuthProvider.getCredential(account!!.idToken, null)
        repository.loginWithGoogle(authCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
                context.showToast("Authentication Error")
            }
        }
    }

    fun fetchUserDetails(): Task<DocumentSnapshot> {
        return repository.fetchUserDetails()
    }

    fun updateUserProfile(selectedPhotoUri: Uri?) {
        repository.updateUserprofile(selectedPhotoUri)

    }

    fun logout() {
        repository.logout()
    }


    fun addCategory(categoryName: String, selectedPhotoUri: Uri) {
        repository.addCategory(categoryName, selectedPhotoUri)
    }

    fun fetchCategories(): LiveData<List<Category>> {
        categoryList = mutableListOf()
        if (categoryList.size > 0) {
            categoryList.clear()
        }
        repository.fetchCategories()
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (documentChange: DocumentChange in snapshot.documentChanges) {
                        documentChange.document.data
                        val fetchedCategory = Category(
                            documentChange.document.get("categoryName").toString(),
                            documentChange.document.get("categoryImage").toString())
                        categoryList.add(fetchedCategory)
                    }
                    savedUserCategories.value = categoryList
                } else {
                    Log.d("TAG", "Query snapshot is null")
                }
            }
        return savedUserCategories
    }

    fun addPhotos(selectedPhotoUri: Uri, timeInMilis: String, date: String, categoryName: String) {
        repository.addPhotos(selectedPhotoUri, timeInMilis, date, categoryName)
    }

    fun fetchPhotos(categoryName: String): LiveData<List<Photos>> {
        photosList =  mutableListOf()
        if (photosList.size > 0) {
            photosList.clear()
        }
        repository.fetchPhotos(categoryName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (documentChange: DocumentChange in snapshot.documentChanges) {
                        val fetchedPhotos = Photos(
                            documentChange.document.getString("image")!!,
                            documentChange.document.getString("time")!!,
                            documentChange.document.getString("date")!!
                        )
                        photosList.add(fetchedPhotos)
                    }
                    savedUserPhotos.value = photosList
                } else {
                    Log.d("TAG", "Query snapshot is null")
                }
            }
        return savedUserPhotos
    }

    fun deleteImage(image: String, categoryName: String, timeInMilis: String) {
        repository.deleteImage(image, categoryName, timeInMilis)
    }

    fun fetchTimeLine(): LiveData<List<TimeLineModel>> {
        repository.fetchTimeLine().listAll()
            .addOnSuccessListener {
                val timeLineList = mutableListOf<TimeLineModel>()
                for (i in it.items) {
                    i.metadata.addOnSuccessListener {
                        val timeLineModel = TimeLineModel(i.downloadUrl, it.creationTimeMillis)
                        timeLineList.add(timeLineModel)

                        tList = timeLineList.sortedByDescending {
                            it.timeStamp as Long
                        }
                        savedTimeLinePhotos.value = tList
                        Log.d("image",savedTimeLinePhotos.value.toString())

                    }
                }

            }
        return savedTimeLinePhotos
    }
}