package com.rnkbirdhaven.bird_haven

import androidx.lifecycle.ViewModel

//ViewModel class for sharing and managing user-selected settings and preferences(GeekforGeeks,2022)
class SharedViewModel : ViewModel() {
    //Default unit for selected distance(The IIE,2023)
    var selectedDistanceUnit: String = "Imperial System(KM)"

    //Default preferred distance(The IIE,2023)
    var selectedPreferredDistance: Int = 0

    //Method to update the preferred distance(The IIE,2023)
    fun updatePreferredDistance(preferredDistance: Int) {
        selectedPreferredDistance = preferredDistance
    }
}
