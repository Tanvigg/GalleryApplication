package com.example.galleryapplication.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.galleryapplication.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wang.avi.AVLoadingIndicatorView

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private lateinit var categoryList: List<Category>
    var context: Context
    var listener: CategoryClickListener


    constructor(context: Context,listener: CategoryClickListener
    ) : super() {
        this.context = context
        this.listener =listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowlayout: View = layoutInflater.inflate(R.layout.category_row_layout, parent, false)
        return CategoryViewHolder(
            rowlayout
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryItem = categoryList[position]
        holder.categoryName.text = categoryItem.categoryName
        holder.imageProgressBar.visibility = View.VISIBLE
        Picasso.get().load(categoryItem.categoryImage)
            .into(holder.categoryImage, object : Callback {
                override fun onSuccess() {
                    Log.d("imageUpload", "success")
                    holder.imageProgressBar.visibility = View.GONE

                }

                override fun onError(e: Exception?) {
                    Log.d("imageUpload", "failed")
                    holder.imageProgressBar.visibility = View.GONE

                }

            })
        holder.categoryImage.setOnClickListener {
            listener.onCategoryClick(categoryItem.categoryName)
        }
    }


    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setCategory(fetchedCategory :List<Category>){
        categoryList = fetchedCategory
        notifyDataSetChanged()

    }



    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryName: TextView = itemView.findViewById(R.id.category_name)
        var categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        var imageProgressBar: AVLoadingIndicatorView = itemView.findViewById(R.id.ballpulse)

    }


}

