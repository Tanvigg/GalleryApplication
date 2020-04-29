package com.example.galleryapplication.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.galleryapplication.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class TimeLineAdapter : RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder> {
    private lateinit var timeLineList: List<TimeLineModel>
    var context: Context

    constructor(context: Context) : super() {
        this.context = context
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
        holder.date.setText(timeDD)
    }

    override fun getItemCount(): Int {
        return timeLineList.size
    }

    fun setImage(timeLine: List<TimeLineModel>){
        timeLineList = timeLine.sortedByDescending {
            it.timeStamp as Long
        }
        notifyDataSetChanged()
    }

    class TimeLineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeline_image: ImageView = itemView.findViewById(R.id.timeline_image)
        val date: TextView = itemView.findViewById(R.id.date)


    }


}