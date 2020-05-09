package com.example.galleryapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.galleryapplication.model.Repository
import com.example.galleryapplication.model.TimeLineModel
import com.example.galleryapplication.model.isNetworkAvailable

class TimeLineViewModel(val context: Application) : AndroidViewModel(context) {
    private val repository = Repository()
    private var savedTimeLinePhotos: MutableLiveData<List<TimeLineModel>> = MutableLiveData()
    private lateinit var tList:List<TimeLineModel>
    private var errMessage = MutableLiveData<String>()
    private var timeLineStatus = MutableLiveData<TimeLineStatus>()

    fun getError(): LiveData<String> {
        return errMessage
    }

    fun getTimeLineStatus(): LiveData<TimeLineStatus> {
        return timeLineStatus
    }

    fun fetchTimeLine(): LiveData<List<TimeLineModel>> {
        timeLineStatus.value = TimeLineStatus.SHOW_PROGRESS

        if (!(context.isNetworkAvailable())) {
            errMessage.value = "Network not available"
        }
        repository.fetchTimeLine().listAll()
            .addOnSuccessListener {
                if (it != null) {
                    timeLineStatus.value = TimeLineStatus.HIDE_PROGRESS

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
                            Log.d("image", savedTimeLinePhotos.value.toString())
                        }
                    }
                } else {
                    errMessage.value = it.toString()
                    timeLineStatus.value = TimeLineStatus.HIDE_PROGRESS
                }
            }

        return savedTimeLinePhotos
    }


    enum class TimeLineStatus {
        SHOW_PROGRESS,
        HIDE_PROGRESS,
    }
}