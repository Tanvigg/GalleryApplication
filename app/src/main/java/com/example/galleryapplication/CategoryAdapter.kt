package com.example.galleryapplication

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    var categoryList: ArrayList<Category>
    var context: Context
    var listener: CategoryClickListener

    constructor(
        context: Context,
        categoryList: ArrayList<Category>,
        listener: CategoryClickListener
    ) : super() {
        this.categoryList = categoryList
        this.context = context
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowlayout: View = layoutInflater.inflate(R.layout.category_row_layout, parent, false)
        return CategoryViewHolder(rowlayout)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryItem = categoryList[position]
        holder.categoryName.text = categoryItem.categoryName
        Picasso.get().load(categoryItem.categoryImage).into(holder.categoryImage)

        holder.categoryImage.setOnClickListener {
            listener.onCategoryClick(categoryItem.categoryName)
        }


    }


    override fun getItemCount(): Int {
        return categoryList.size
    }




    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryName: TextView = itemView.findViewById(R.id.category_name)
        var categoryImage: ImageView = itemView.findViewById(R.id.category_image)
    }


}

