package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.galleryapplication.model.Repository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class ProfileViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()

    fun fetchUserDetails(): Task<DocumentSnapshot> {
        return repository.fetchUserDetails()
    }

    fun updateUserProfile(selectedPhotoUri: Uri?) {
        repository.updateUserprofile(selectedPhotoUri)

    }

    fun logout() {
        repository.logout()
    }

}