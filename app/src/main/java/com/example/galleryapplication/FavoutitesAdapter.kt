package com.example.galleryapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class FavoutitesAdapter : RecyclerView.Adapter<FavoutitesAdapter.FavouritesViewHolder> {
     var context: Context
     var favList: ArrayList<FavouritesModel>

    constructor(context: Context, favList: ArrayList<FavouritesModel>) : super() {
        this.favList = favList
        this.context= context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
        return FavouritesViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.favourites_row_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        var favItem = favList[position]
        Picasso.get().load(favItem.image).into(holder.image)
    }

    override fun getItemCount(): Int {
        return favList.size
    }



    class FavouritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.fav_image)

    }
}