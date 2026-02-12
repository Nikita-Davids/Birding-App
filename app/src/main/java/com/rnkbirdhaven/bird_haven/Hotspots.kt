package com.rnkbirdhaven.bird_haven

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class Hotspots : Fragment(), OnMapReadyCallback {

    //Declaring variables(The IIE,2023)
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val eBirdApiKey = "qha94dkmp6ve"
    private val KILOMETERS_TO_MILES = 0.621371
    private var selectedDistanceKm: Double = 10.0
    private lateinit var sharedViewModel: SharedViewModel
    private var currentLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_hotspots, container, false)

        //If the supportActionBar object is not null, the hide() method is called to hide the support action bar(see Splash Screen - Android Studio,2020)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.hide()

        //Initializing the MapView(The IIE,2023)
        mapView = layout.findViewById(R.id.mapView)

        //Creating and initialize the MapView (used for displaying maps)(The IIE,2023)
        mapView.onCreate(savedInstanceState)

        //Getting a reference to the Google Map and set up the map callback(The IIE,2023)
        mapView.getMapAsync(this)

        //Initializing the FusedLocationProviderClient (used for accessing device location)(The IIE,2023)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        //Initializing the SharedViewModel (used for sharing data between fragments(Hotspots and Settings))(The IIE,2023)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        //Creating the SettingsFragment instance(The IIE,2023)
        val settingsFragment = Settings()

        //Starting the SettingsFragment without adding it to the UI stack(The IIE,2023)
        fragmentTransaction.add(settingsFragment, "SettingsFragmentTag")
        fragmentTransaction.commit()

        return layout
    }

    override fun onMapReady(map: GoogleMap) {
        //Assigning the GoogleMap object to the 'googleMap' property(The IIE,2023)
        googleMap = map

        //Checking if the app has the necessary location permission(The IIE,2023)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Enabling the "My Location" feature on the GoogleMap(The IIE,2023)
            googleMap.isMyLocationEnabled = true

            //Retrieving the last known location of the device(The IIE,2023)
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                //Checking if the location retrieval was successful and a result is available(The IIE,2023)
                if (task.isSuccessful && task.result != null) {
                    //Getting the location result(The IIE,2023)
                    val location = task.result

                    //Creating a LatLng object representing the current location(The IIE,2023)
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    //Moving the camera to the current location with a zoom level of 10(The IIE,2023)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                    //Retrieving the preferred distance from the SharedViewModel(The IIE,2023)
                    val preferredDistance = sharedViewModel.selectedPreferredDistance.toDouble()

                    //Updating the selectedDistanceKm variable with the preferred distance(The IIE,2023)
                    selectedDistanceKm = preferredDistance

                    //Fetching and displaying eBird hotspots based on the current location(The IIE,2023)
                    fetchAndDisplayEBirdHotspots(currentLatLng)
                }
            }
        } else {
            //Requesting location permission if it is not granted(The IIE,2023)
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_FINE_LOCATION
            )
            //Calling refreshExploreFragment(The IIE,2023)
            refreshExploreFragment()
        }
    }

    //Function to reload explore fragment(The IIE,2023)
    private fun refreshExploreFragment() {
        //Replace or reload the "explore" fragment as needed(The IIE,2023)
        val fragment = Explore()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    //Function to fetch and display hotspots(Postman,N/A)
    private fun fetchAndDisplayEBirdHotspots(currentLatLng: LatLng) {
        //Getting the selected system (Metric System or Imperial System) from SharedViewModel(The IIE,2023)
        val selectedSystem = sharedViewModel.selectedDistanceUnit

        //Determining if the selected system is the Metric System (Miles)(The IIE,2023)
        val isMetricSystem = selectedSystem == "Metric System(Miles)"

        //Defining the distance unit based on the selected system(The IIE,2023)
        val distUnit: String = if (isMetricSystem) {
            "miles" //Using "miles" when Metric System is selected(The IIE,2023)
        } else {
            "km"    //Using "km" when Imperial System is selected(The IIE,2023)
        }

        //Calculating the selected distance based on the selected system(The IIE,2023)
        val selectedDistance = if (isMetricSystem) {
            selectedDistanceKm * KILOMETERS_TO_MILES //Converting kilometers to miles(The IIE,2023)
        } else {
            selectedDistanceKm //Using selected distance as is (in kilometers) for Imperial System(KM)(The IIE,2023)
        }

        //Building the API URL for fetching eBird hotspots data(Postman,N/A)
        val apiUrl = "https://api.ebird.org/v2/data/obs/geo/recent" +
                "?lat=${currentLatLng.latitude}" +
                "&lng=${currentLatLng.longitude}" +
                "&maxResults=$MAX_RESULTS" +
                "&dist=$selectedDistance" +
                "&distType=$distUnit" +
                "&key=$eBirdApiKey"

        //Starting a new thread to make the API request(The IIE,2023)
        Thread {
            try {
                //Creating a URL object from the API URL string(The IIE,2023)
                val url = URL(apiUrl)

                //Opening a connection to the URL (HTTP GET request)(The IIE,2023)
                val connection = url.openConnection() as HttpURLConnection

                //Setting the HTTP request method to GET(The IIE,2023)
                connection.requestMethod = "GET"

                //Getting the HTTP response code
                val responseCode = connection.responseCode

                //Checking if the response code indicates success (HTTP OK)(The IIE,2023)
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //Reading the response data from the connection(The IIE,2O23)
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    //Parsing the JSON response to obtain a list of eBird hotspots(The IIE,2023)
                    val hotspots = parseHotspotData(response)

                    //Updating the map UI on the main (UI) thread(The IIE,2023)
                    requireActivity().runOnUiThread {
                        //Clearing the existing markers on the map(The IIE,2023)
                        googleMap.clear()

                        //Adding markers for each eBird hotspot to the map(Community Bot,2017)
                        hotspots.forEach { hotspot ->
                            val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(hotspotLatLng)
                                    .title(hotspot.locName)
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_YELLOW
                                        )
                                    )
                            )
                        }
                    }
                } else {
                    //Handling error response (HTTP status code indicates an error)(The IIE,2023)
                }
            } catch (e: Exception) {
                //Handling exceptions that may occur during the network request(The IIE,2023)
                e.printStackTrace()
            }
        }.start() //Starting the thread to fetch and display eBird hotspots(The IIE,2023)
    }


    //Public function to update the displayed eBird hotspots on the map based on the selected distance(The IIE,2023)
    fun updateDisplayedHotspots(selectedDistance: Int) {
        //Updating the selectedDistanceKm variable with the selected distance in kilometers(The IIE,2023)
        selectedDistanceKm = selectedDistance.toDouble()

        //Checking if the currentLatLng is not null (user's current location is available)(The IIE,2023)
        currentLatLng?.let {
            //Calling the fetchAndDisplayEBirdHotspots method to fetch and display hotspots(The IIE,2023)
            //Using the updated selected distance(The IIE,2023)
            fetchAndDisplayEBirdHotspots(it)
        }
    }

    //The function has a JSON response string from the eBird API(The IIE,2023)
    private fun parseHotspotData(response: String): List<Hotspot> {
        //Creating an empty list to store the parsed hotspots(The IIE,2023)
        val hotspots = mutableListOf<Hotspot>()

        //Creating a JSONArray from the JSON response string(The IIE,2023)
        val jsonArray = JSONArray(response)

        //Iterating through the JSON array to extract hotspot information(The IIE,2023)
        for (i in 0 until jsonArray.length()) {
            //Getting the JSON object at the current position in the array(The IIE,2023)
            val jsonObject = jsonArray.getJSONObject(i)

            //Extracting hotspot information from the JSON object(The IIE,2023)
            val hotspotId = jsonObject.getString("locId")
            val hotspotName = jsonObject.getString("locName")
            val latitude = jsonObject.getDouble("lat")
            val longitude = jsonObject.getDouble("lng")

            //Creating a Hotspot object with the extracted information and add it to the list(The IIE,2023)
            hotspots.add(Hotspot(hotspotId, hotspotName, latitude, longitude))
        }

        //Returning the list of parsed hotspots(The IIE,2023)
        return hotspots
    }

    //Called when the fragment is resumed. Resumes the MapView(Developers,2023)
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    //Called when the fragment is paused. Pauses the MapView(Developers,2023)
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    // Called when the fragment is destroyed. Destroys the MapView(Developers,2023)
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    //Called when the system is running low on memory. Notifies the MapView of low memory conditions(Developers,2023)
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    //Companion object defines constants for location permission and API requests(Mishra,2016)
    companion object {
        //Request code for fine location permission. This code is used when requesting permission to access fine location(The IIE,2023)
        private const val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1

        //Maximum number of results to retrieve from the eBird API. Adjust this value as needed for the maximum number of hotspots to retrieve(The IIE,2023)
        private const val MAX_RESULTS = 100
    }
}