package com.example.galleryapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_gallery.*

class GalleryActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val manager: FragmentManager = supportFragmentManager
    private val transaction = manager.beginTransaction()
    private val categoryFragment = CategoryFragment()
    private val timeLineFragment = TimeLineFragment()
    private val favouritesFragment = FavouritesFragment()
    private val profileFragment = ProfileFragment()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)


        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Categories")


        bottomNav.setOnNavigationItemSelectedListener(this)
        transaction.replace(R.id.container,categoryFragment)
        transaction.commit()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var manager1 = supportFragmentManager
        var transaction1 : FragmentTransaction = manager1.beginTransaction()

        if(item.itemId == R.id.category){
            supportActionBar!!.setTitle("Categories")
            transaction1.replace(R.id.container,categoryFragment)
        }

        else if(item.itemId == R.id.timeline){
            supportActionBar!!.setTitle("Your Timeline")
            transaction1.replace(R.id.container,timeLineFragment)
        }
        else if(item.itemId == R.id.favourites){
            supportActionBar!!.setTitle("Favourites")
            transaction1.replace(R.id.container,favouritesFragment)
        }
        else if(item.itemId ==  R.id.profile){
            supportActionBar!!.setTitle("Profile")
            transaction1.replace(R.id.container,profileFragment)
        }

        transaction1.commit()
        return true

    }
}
