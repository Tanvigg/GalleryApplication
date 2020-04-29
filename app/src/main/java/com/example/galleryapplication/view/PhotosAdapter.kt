package com.example.galleryapplication.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.galleryapplication.R
import com.squareup.picasso.Picasso

class PhotosAdapter : RecyclerView.Adapter<PhotosAdapter.ImageViewHolder> {
    private lateinit var photosList: List<Photos>
    var context: Context
    var listener : PhotoClickListener

    constructor(context: Context, listener: PhotoClickListener) : super() {
        this.context = context
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowlayout: View = layoutInflater.inflate(R.layout.image_row_layout, parent, false)
        return ImageViewHolder(
            rowlayout
        )


    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int){
        val imageItem = photosList[position]
        Picasso.get().load(imageItem.image).into(holder.image)

        holder.image.setOnClickListener{
            listener.onPhotoClick(imageItem.time,imageItem.date,imageItem.image)

        }
    }

    override fun getItemCount(): Int {
       return photosList.size
    }

    fun setPhotoData(fetchedPhotos: List<Photos>) {
        photosList = fetchedPhotos
        notifyDataSetChanged()
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)

    }


}