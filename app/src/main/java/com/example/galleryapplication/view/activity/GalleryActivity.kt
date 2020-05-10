package com.example.galleryapplication.view.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.galleryapplication.R
import com.example.galleryapplication.view.fragment.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.content_gallery.*

class GalleryActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener ,
    CategoryFragment.OnDataPass,
    PhotosFragment.OnDataPass {
    private val manager: FragmentManager = supportFragmentManager
    private val transaction = manager.beginTransaction()
    private val categoryFragment = CategoryFragment()
    private val timeLineFragment = TimeLineFragment()
    private val profileFragment = ProfileFragment()
    private var photosFragment = PhotosFragment()
    private var imageViewerFragment = ImageViewerFragment()
    private lateinit var catName : String
    private lateinit var date : String

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
            transaction1.replace(R.id.container,categoryFragment).addToBackStack(null)
        }

        else if(item.itemId == R.id.timeline){
            supportActionBar!!.title = "Your Timeline"
            transaction1.replace(R.id.container,timeLineFragment).addToBackStack(null)
        }

        else if(item.itemId == R.id.profile){
            supportActionBar!!.title = "Profile"
            transaction1.replace(R.id.container,profileFragment).addToBackStack(null)
        }

        transaction1.commit()
        return true

    }

    override fun onBackPressed() {
        Log.i("LOG", "BACK PRESS")
        val manager = supportFragmentManager
        val list: List<Fragment> = manager.fragments
        Log.i("Backstack count", manager.backStackEntryCount.toString())
        if (manager.backStackEntryCount > 0) {
            super.onBackPressed()
            val currentFragment = manager.findFragmentById(R.id.container)
            Log.i("Current Fragment", currentFragment.toString())
            if (currentFragment is CategoryFragment) {
                bottomNav.menu.getItem(0).setChecked(true)
                supportActionBar!!.title = "Categories"
            } else if (currentFragment is TimeLineFragment) {
                bottomNav.menu.getItem(1).setChecked(true)
            } else if (currentFragment is ProfileFragment) {
                bottomNav.menu.getItem(2).setChecked(true)
            } else if (currentFragment is ImageViewerFragment){
                bottomNav.menu.getItem(0).setChecked(true)
                supportActionBar!!.title = date
            } else if (currentFragment is PhotosFragment) {
                bottomNav.menu.getItem(0).setChecked(true)
                supportActionBar!!.title = catName
            }
        }
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
        date = currentDate
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
