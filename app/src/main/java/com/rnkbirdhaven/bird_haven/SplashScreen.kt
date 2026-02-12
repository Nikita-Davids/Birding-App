package com.rnkbirdhaven.bird_haven

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreen : AppCompatActivity() {
    private val SPLASH_DURATION: Long = 5000 //5 seconds (see Splash Screen - Android Studio,2020)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        //Use a handler to delay the splash screen and then start the main activity (see Splash Screen - Android Studio,2020)
        Handler().postDelayed({
            //Start the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            //Close the splash activity so the user won't go back to it using the back button (see Splash Screen - Android Studio,2020)
            finish()
        }, SPLASH_DURATION)
    }
}