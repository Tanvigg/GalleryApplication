package com.example.galleryapplication.model

class Category(var categoryName: String, var categoryImage: String) {

    override fun toString(): String {
        return "Category(categoryName='$categoryName', categoryImage='$categoryImage')"
    }

}