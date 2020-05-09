package com.example.galleryapplication.model
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Toast



fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.isNetworkAvailable() : Boolean{
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
}

fun getImageUri(context: Context, inImage: Bitmap): Uri {
    val outImage: Bitmap = Bitmap.createScaledBitmap(inImage, 2000, 2000, true)
    val path: String =
        MediaStore.Images.Media.insertImage(context.contentResolver, outImage, "Title", null)
    return Uri.parse(path)
}


