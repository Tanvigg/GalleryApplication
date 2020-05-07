package com.example.galleryapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.galleryapplication.model.Repository

class ImageViewerViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()

    fun deleteImage(image: String, categoryName: String, timeInMilis: String) {
        repository.deleteImage(image, categoryName, timeInMilis)
    }

}