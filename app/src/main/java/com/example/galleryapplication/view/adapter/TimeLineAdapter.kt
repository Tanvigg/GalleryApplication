package com.example.galleryapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.galleryapplication.R
import com.example.galleryapplication.model.TimeLineModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.timeline_rowlayout.view.*
import java.text.SimpleDateFormat
import java.util.*

class TimeLineAdapter : RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder> {
    var timeLineList: List<TimeLineModel>
    var context: Context

    constructor(timeLineList: List<TimeLineModel>, context: Context) : super() {
        this.context = context
        this.timeLineList = timeLineList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        return TimeLineViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.timeline_rowlayout,
                parent,
                false
            )
        )


    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        timeLineList[position].url.addOnSuccessListener {
            Picasso.get().load(it).into(holder.timeline_image)
        }
        val formatter = SimpleDateFormat("MMM dd, hh:mm aaa")
        val timeDD: String = formatter.format(Date(timeLineList[position].timeStamp))
        holder.date.text = timeDD
    }

    override fun getItemCount(): Int {
        return timeLineList.size
    }

    class TimeLineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeline_image: ImageView = itemView.timeline_image
        val date: TextView = itemView.date


    }


}