package com.rnkbirdhaven.bird_haven

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout for this fragment(The IIE,2023)
        val layout = inflater.inflate(R.layout.fragment_home, container, false)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.hide()

        //Find the button by its ID(The IIE,2023)
        val birdClassifierButton: Button = layout.findViewById(R.id.btnBirdClassifier)

        birdClassifierButton.setOnClickListener {
            //Create an Intent to navigate to the BirdClassifier activity(The IIE,2023)
            val intent = Intent(requireContext(), BirdClassifier::class.java)
            startActivity(intent)
        }

        //Find the button by its ID(The IIE,2023)
        val checkWeather: Button = layout.findViewById(R.id.btnCheckWeather)

        checkWeather.setOnClickListener {
            //Create an Intent to navigate to the Weather activity(The IIE,2023)
            val intent = Intent(requireContext(), Weather::class.java)
            startActivity(intent)
        }

        return layout
    }
}
