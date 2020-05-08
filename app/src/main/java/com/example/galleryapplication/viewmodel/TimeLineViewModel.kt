package com.example.galleryapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.view.Model.TimeLineModel

class TimeLineViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var savedTimeLinePhotos: MutableLiveData<List<TimeLineModel>> = MutableLiveData()
    private lateinit var tList:List<TimeLineModel>


    fun fetchTimeLine(): LiveData<List<TimeLineModel>> {
        repository.fetchTimeLine().listAll()
            .addOnSuccessListener {
                val timeLineList = mutableListOf<TimeLineModel>()
                for (i in it.items) {
                    i.metadata.addOnSuccessListener {
                        val timeLineModel =
                            TimeLineModel(
                                i.downloadUrl,
                                it.creationTimeMillis
                            )
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