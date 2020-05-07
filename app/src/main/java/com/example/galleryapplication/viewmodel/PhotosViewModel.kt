package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.Photos
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class PhotosViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var savedUserPhotos: MutableLiveData<List<Photos>> = MutableLiveData()



    fun addPhotos(selectedPhotoUri: Uri, timeInMilis: String, date: String, categoryName: String) {
        repository.addPhotos(selectedPhotoUri, timeInMilis, date, categoryName)
    }

    fun fetchPhotos(categoryName: String): LiveData<List<Photos>> {
        repository.fetchPhotos(categoryName)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@EventListener
                }

                val photosList: MutableList<Photos> = mutableListOf()
                for (doc in value!!) {
                    val fetchedPhotos = Photos(
                        doc.getString("image")!!,
                        doc.getString("time")!!,
                        doc.getString("date")!!
                    )
                    photosList.add(fetchedPhotos)
                }
                savedUserPhotos.value = photosList

            })
        return savedUserPhotos
    }
}