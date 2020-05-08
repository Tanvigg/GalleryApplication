package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.Category
import com.example.galleryapplication.view.isNetworkAvailable
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class CategoryViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var savedUserCategories: MutableLiveData<List<Category>> = MutableLiveData()
    private var categoryStatus = MediatorLiveData<CategoryStatus>()
    private var errMessage = MutableLiveData<String>()

    fun getError(): LiveData<String> {
        return errMessage
    }

    fun getCategoryStatus() : LiveData<CategoryStatus>{
        return categoryStatus
    }

    fun addCategory(categoryName: String, selectedPhotoUri: Uri){
        if (!(context.isNetworkAvailable())) {
            errMessage.value = "Network not available"
            return
        }
        categoryStatus.value = CategoryStatus.SHOW_PROGRESS
        categoryStatus.addSource(repository.addCategory(categoryName, selectedPhotoUri), Observer {
            it.onSuccess {
                categoryStatus.value = CategoryStatus.HIDE_PROGRESS

            }
            it.onFailure {
                errMessage.value = it.toString()
                categoryStatus.value = CategoryStatus.HIDE_PROGRESS
            }

        })
    }

    fun fetchCategories(): LiveData<List<Category>> {
        repository.fetchCategories()
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@EventListener
                }
                val categoryList: MutableList<Category> = mutableListOf()
                for (doc in value!!) {
                    val fetchedCategory = Category(
                        doc.get("categoryName").toString(),
                        doc.get("categoryImage").toString())
                    categoryList.add(fetchedCategory)
                    categoryStatus.value = CategoryStatus.HIDE_PROGRESS
                }
                savedUserCategories.value = categoryList
            })

        return savedUserCategories
    }

    enum class CategoryStatus{
        HIDE_PROGRESS,
        SHOW_PROGRESS
    }


}