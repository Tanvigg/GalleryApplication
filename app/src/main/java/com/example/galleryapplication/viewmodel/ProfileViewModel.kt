package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.model.isNetworkAvailable
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class ProfileViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var profileStatus = MediatorLiveData<ProfileStatus>()
    private var errMessage = MutableLiveData<String>()


    fun getProfileStatus(): LiveData<ProfileStatus> {
        return profileStatus
    }

    fun getError(): LiveData<String> {
        return errMessage
    }


    fun fetchUserDetails(): Task<DocumentSnapshot> {
        if (!(context.isNetworkAvailable())) {
            errMessage.value = "Network not available"
        }
        profileStatus.value = ProfileStatus.SHOW_PROGRESS
        return repository.fetchUserDetails().addOnSuccessListener {
            profileStatus.value = ProfileStatus.HIDE_PROGRESS
        }
            .addOnFailureListener {
                errMessage.value = it.toString()
                profileStatus.value = ProfileStatus.HIDE_PROGRESS

            }
    }

    fun updateUserProfile(selectedPhotoUri: Uri?) {
        if (!(context.isNetworkAvailable())) {
            errMessage.value = "Network not available"
        }
        profileStatus.value = ProfileStatus.SHOW_PROGRESS
        profileStatus.addSource(repository.updateUserprofile(selectedPhotoUri), Observer {
            it.onSuccess {
                profileStatus.value = ProfileStatus.HIDE_PROGRESS
            }
            it.onFailure {
                errMessage.value = it.toString()
                profileStatus.value = ProfileStatus.HIDE_PROGRESS

            }

        })

    }

    fun logout() {
        repository.logout()
    }

    enum class ProfileStatus {
        SHOW_PROGRESS,
        HIDE_PROGRESS,
    }
}