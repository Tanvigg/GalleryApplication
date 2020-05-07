package com.example.galleryapplication.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.Category
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class CategoryViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var savedUserCategories: MutableLiveData<List<Category>> = MutableLiveData()


    fun addCategory(categoryName: String, selectedPhotoUri: Uri) : Boolean {
        return repository.addCategory(categoryName, selectedPhotoUri)
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
                }
                savedUserCategories.value = categoryList
            })

        return savedUserCategories
    }


}