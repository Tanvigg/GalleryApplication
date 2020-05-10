package com.example.galleryapplication
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.graphics.toColor as toColor1

class MainActivity : AppCompatActivity() {

    private val manager: FragmentManager = supportFragmentManager
    private val transaction = manager.beginTransaction()
    private val loginFragment = LoginFragment()
    private val signupFragment = SignUpFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(" ")
        supportActionBar!!.setIcon(R.drawable.ic_photo_library_black_24dp)
        textView1.setText(R.string.welcome_in)
        textView2.setText(R.string.sign_in)
        transaction.replace(R.id.container, loginFragment).commit()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val manager1: FragmentManager = supportFragmentManager
        val transaction1 = manager1.beginTransaction()
        if (item.itemId == R.id.signin) {
            supportActionBar!!.title = " "
            supportActionBar!!.setIcon(R.drawable.ic_photo_library_black_24dp)
            textView1.setText(R.string.welcome_in)
            textView2.setText(R.string.sign_in)
            transaction1.replace(R.id.container, loginFragment)
        } else if (item.itemId == R.id.signup) {
            supportActionBar!!.title = " "
            supportActionBar!!.setIcon(R.drawable.ic_photo_library_black_24dp)
            textView1.setText(R.string.get_on)
            textView2.setText(R.string.sign_up)
            transaction1.replace(R.id.container, signupFragment)

        }
        transaction1.commit()
        return super.onOptionsItemSelected(item)
    }

}