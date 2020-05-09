package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.model.Photos
import com.example.galleryapplication.model.isNetworkAvailable
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class PhotosViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var savedUserPhotos: MutableLiveData<List<Photos>> = MutableLiveData()
    private var photosStatus = MediatorLiveData<PhotoStatus>()
    private var errMessage = MutableLiveData<String>()


    fun getPhotosStatus(): LiveData<PhotoStatus> {
        return photosStatus
    }

    fun getError(): LiveData<String> {
        return errMessage
    }

    fun addPhotos(selectedPhotoUri: Uri, timeInMilis: String, date: String, categoryName: String) {
        if (!(context.isNetworkAvailable())) {
            errMessage.value = "Network not available"
            return
        }
        photosStatus.value = PhotoStatus.SHOW_PROGRESS
        photosStatus.addSource(
            repository.addPhotos(selectedPhotoUri, timeInMilis, date, categoryName), Observer {
                it.onSuccess {
                    photosStatus.value = PhotoStatus.HIDE_PROGRESS

                }
                it.onFailure {

                    errMessage.value = it.toString()
                    photosStatus.value = PhotoStatus.HIDE_PROGRESS
                }

            })

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
                    val fetchedPhotos =
                        Photos(
                            doc.getString("image")!!,
                            doc.getString("time")!!,
                            doc.getString("date")!!
                        )
                    photosList.add(fetchedPhotos)
                    photosStatus.value = PhotoStatus.HIDE_PROGRESS
                }
                savedUserPhotos.value = photosList

            })
        return savedUserPhotos
    }

    enum class PhotoStatus {
        SHOW_PROGRESS,
        HIDE_PROGRESS,
    }

}