package com.example.galleryapplication.view.Model

import android.net.Uri
import com.google.android.gms.tasks.Task

class TimeLineModel(val url : Task<Uri>, val timeStamp : Long)