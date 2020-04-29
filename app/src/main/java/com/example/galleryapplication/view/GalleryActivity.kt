package com.example.galleryapplication.view

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.galleryapplication.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.content_gallery.*

class GalleryActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener , CategoryFragment.OnDataPass ,
    PhotosFragment.OnDataPass {
    private val manager: FragmentManager = supportFragmentManager
    private val transaction = manager.beginTransaction()
    private val categoryFragment = CategoryFragment()
    private val timeLineFragment = TimeLineFragment()
    private val profileFragment = ProfileFragment()
    private var photosFragment = PhotosFragment()
    private var imageViewerFragment = ImageViewerFragment()
    private lateinit var catName : String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        setSupportActionBar(toolbar_gallery)
        supportActionBar!!.title = "Categories"
        bottomNav.setOnNavigationItemSelectedListener(this)
        transaction.replace(R.id.container,categoryFragment)
        transaction.commit()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val manager1 = supportFragmentManager
        val transaction1 : FragmentTransaction = manager1.beginTransaction()

        if(item.itemId == R.id.category){
            supportActionBar!!.setTitle("Categories")
            transaction1.replace(R.id.container,categoryFragment)
        }

        else if(item.itemId == R.id.timeline){
            supportActionBar!!.title = "Your Timeline"
            transaction1.replace(R.id.container,timeLineFragment)
        }

        else if(item.itemId == R.id.profile){
            supportActionBar!!.title = "Profile"
            transaction1.replace(R.id.container,profileFragment)
        }

        transaction1.commit()
        return true

    }

    override fun sendCategoryName(categoryName: String) {
        catName = categoryName
        val bundle = Bundle()

        bundle.putString("CategoryName",categoryName)
        photosFragment.arguments = bundle
        supportActionBar!!.title = categoryName
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.container, photosFragment)
        transaction.addToBackStack(null) // if written, this transaction will be added to backstack
        transaction.commit()

    }

    override fun sendCurrentTime(currentTime: String,currentDate : String,image :String,categoryName: String) {
        val bundle = Bundle()
        bundle.putString("Time",currentTime)
        bundle.putString("Date",currentDate)
        bundle.putString("Image",image)
        bundle.putString("CategoryName",categoryName)
        Log.d("data",currentDate)

        imageViewerFragment.arguments = bundle
        supportActionBar!!.title = currentDate
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,imageViewerFragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }
}
