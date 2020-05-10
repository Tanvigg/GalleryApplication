package com.example.galleryapplication.model

import android.app.Activity
import android.app.AlertDialog
import com.example.galleryapplication.R
import kotlinx.android.synthetic.main.custom_dialog_progress.view.*

class LoadingDialog(activity:Activity) {
    private val mActivity = activity
    private lateinit var dialog: AlertDialog
    fun startLoadingDialog(dialogMessage1 : String,dialogMessage2: String){
        val builder = AlertDialog.Builder(mActivity)
        val inflater = mActivity.layoutInflater
        val view = inflater.inflate(R.layout.custom_dialog_progress,null)
        val text1 = view.progressText1
        val text2 = view.progressText
        text1.text = dialogMessage1
        text2.text = dialogMessage2
        builder.setView(view)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }
     fun dismissDialog(){
         dialog.dismiss()
     }
}