package com.rnkbirdhaven.bird_haven

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.rnkbirdhaven.bird_haven.databinding.ActivityWelcomeBinding

class Welcome : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root) //Setting the content view with the binding root(see Bottom Navigation Bar - Android Studio, 2022)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        supportActionBar?.hide()

        //Initially replace with the Home fragment(see Bottom Navigation Bar - Android Studio, 2022)
        replace(Home())

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replace(Home())
                R.id.explore -> replace(Explore())
                R.id.sighting -> replace(Sightings())
                R.id.observation -> replace(Observation())
                R.id.settings -> replace(Settings())

                else -> {

                }
            }
            true
        }
    }

    override fun onBackPressed() {
        //Declaring and initializing variables(The IIE,2023)
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)

        //If the current fragment is the Home fragment, finish the activity(The IIE,2023)
        if (currentFragment is Home) {
            finish()
        } else {
            //If the current fragment is not Home, replace it with the Home fragment(The IIE,2023)
            replace(Home())
            binding.bottomNavigationView.selectedItemId = R.id.home
        }
    }


    private fun replace(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment) //Replace the fragment in a container(see Bottom Navigation Bar - Android Studio, 2022)
        fragmentTransaction.addToBackStack(null) //Add transaction to back stack for back navigation(see Bottom Navigation Bar - Android Studio, 2022)
        fragmentTransaction.commit() //Commit the transaction(see Bottom Navigation Bar - Android Studio, 2022)
    }
}
