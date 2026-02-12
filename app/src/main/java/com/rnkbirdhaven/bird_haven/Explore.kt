package com.rnkbirdhaven.bird_haven

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Explore : Fragment(), OnMapReadyCallback {

    //Declaring variables(The IIE,2023)
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedHotspotLatLng: LatLng? = null
    private var selectedHotspotName: String? = null
    private val hotspotMap = mutableMapOf<String, Hotspot>()
    private val eBirdApiKey = "qha94dkmp6ve"
    private val KILOMETERS_TO_MILES = 0.621371
    private var selectedDistanceKm: Double = 10.0
    private lateinit var sharedViewModel: SharedViewModel
    private var currentLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_explore, container, false)

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

        //Declaring and initializing button(The IIE,2023)
        val btnClearBestRoute = layout.findViewById<Button>(R.id.btnClearBestRoute)

        //Set a click listener for the "Clear" button(The IIE,2023)
        btnClearBestRoute.setOnClickListener {
            googleMap.clear()
            //Checking location permission and enable My Location(The IIE,2023)
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true

                //Getting last known location and display on map(The IIE,2023)
                fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        val currentLatLng = LatLng(location.latitude, location.longitude)

                        //Moving the camera to the current location with a zoom level of 10(The IIE,2023)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                        //Retrieving the preferred distance from the SharedViewModel(The IIE,2023)
                        val preferredDistance = sharedViewModel.selectedPreferredDistance.toDouble()

                        //Updating the selectedDistanceKm variable with the preferred distance(The IIE,2023)
                        selectedDistanceKm = preferredDistance

                        //Fetch and display eBird hotspot data with the initial distance(The IIE,2023)
                        fetchAndDisplayEBirdHotspots(currentLatLng)
                    }
                }
            } else {
                //Requesting location permission(The IIE,2023)
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }

        return layout
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

    override fun onMapReady(map: GoogleMap) {
        // Assigning the Google Map instance to the local variable 'googleMap'(The IIE,2023)
        googleMap = map

        // Setting a marker click listener for the Google Map(The IIE,2023)
        googleMap.setOnMarkerClickListener { marker ->

            val selectedHotspot = hotspotMap[marker.title]
            val locationName = marker.title

            //Getting the coordinates of the clicked marker(The IIE,2023)
            val markerPosition = marker.position

            //Converting the marker's position to screen coordinates(The IIE,2023)
            val point = googleMap.projection.toScreenLocation(markerPosition)

            // Calculate the offset for the Toast (adjust these values to suit your layout)(The IIE,2023)
            val xOffset = -100 //Adjusting this value to move the Toast horizontally(The IIE,2023)
            val yOffset = 150 //Adjusting this value to move the Toast vertically(The IIE,2023)

            // Display the Toast at the calculated position
            val toast = Toast.makeText(
                requireContext(),
                "Hotspot Location: $locationName",
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.TOP or Gravity.START, point.x + xOffset, point.y + yOffset)
            toast.show()
            if (selectedHotspot != null) {
                // Updating the selected hotspot's latitude and longitude(The IIE,2023)
                selectedHotspotLatLng = LatLng(selectedHotspot.lat, selectedHotspot.lng)
                selectedHotspotName = selectedHotspot.locName

                //Clearing all existing markers on the map(The IIE,2023)
                googleMap.clear()

                //Iterating through all hotspots in the 'hotspotMap'(The IIE,2023)
                hotspotMap.forEach { (title, hotspot) ->
                    val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                    val markerOptions = MarkerOptions()
                        .position(hotspotLatLng)
                        .title(hotspot.locName)

                    if (title == selectedHotspotName) {
                        //Using a blue marker for the selected hotspot(Community Bot,2017)
                        markerOptions.icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    } else {
                        //Using a yellow marker for other hotspots(Community Bot,2017)
                        markerOptions.icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                        )
                    }

                    //Adding the marker to the map(Community Bot,2017)
                    googleMap.addMarker(markerOptions)
                }

                //Draw the route to the selected hotspot//Calling function to draw the route(The IIE,2023)
                drawRouteToSelectedHotspot(selectedHotspotLatLng!!)
            }
            true
        }

        //Checking location permission and enable My Location(The IIE,2023)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true

            //Getting last known location and display on map(The IIE,2023)
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    //Moving the camera to the current location with a zoom level of 10(The IIE,2023)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                    //Retrieving the preferred distance from the SharedViewModel(The IIE,2023)
                    val preferredDistance = sharedViewModel.selectedPreferredDistance.toDouble()

                    //Updating the selectedDistanceKm variable with the preferred distance(The IIE,2023)
                    selectedDistanceKm = preferredDistance

                    //Fetch and display eBird hotspot data with the initial distance(The IIE,2023)
                    fetchAndDisplayEBirdHotspots(currentLatLng)
                }
            }
        } else {
            //Requesting location permission(The IIE,2023)
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
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

        //Defining the distance unit based on the selected system
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
                "&maxResults=${MAX_RESULTS}" +
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

                //Getting the HTTP response code(The IIE,2023)
                val responseCode = connection.responseCode

                //Checking if the response code indicates success (HTTP OK)(The IIE,2023)
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //Reading the response data from the connection(The IIE,2023)
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    //Parsing the JSON response to obtain a list of eBird hotspots(The IIE,2023)
                    val hotspots = parseHotspotData(response)

                    //Updating the map UI on the main (UI) thread(The IIE,2023)
                    requireActivity().runOnUiThread {
                        // Clear the existing markers on the Google Map(The IIE,2023)
                        googleMap.clear()

                        //Clearing the existing markers on the map(The IIE,2023)
                        hotspotMap.clear()

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

                            //Populating the hotspotMap with hotspot information(The IIE,2023)
                            hotspotMap[hotspot.locName] = hotspot
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

    //Function to draw a route to the selected hotspot on the Google Map(Google Maps Platform,2023)
    private fun drawRouteToSelectedHotspot(hotspotLatLng: LatLng) {
        //Clearing the existing markers and routes on the Google Map(The IIE,2023)
        googleMap.clear()

        //Adding a marker for the selected hotspot's location with a blue icon(Community Bot,2017)
        googleMap.addMarker(
            MarkerOptions()
                .position(hotspotLatLng)
                .title(selectedHotspotName) //Setting the marker title to the hotspot's name(Community Bot,2017)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        //Initiating navigation to the selected hotspot using a custom function(The IIE,2023)
        startNavigationToHotspot(hotspotLatLng)
    }

    //Function to parse hotspot data from a JSON response string and return a list of Hotspot objects(The IIE,2023)
    private fun parseHotspotData(response: String): List<Hotspot> {
        //Creating an empty list to store the parsed hotspots(The IIE,2023)
        val hotspots = mutableListOf<Hotspot>()

        try {
            //Creating a JSON array from the response string(The IIE,2023)
            val jsonArray = JSONArray(response)

            //Iterating through the JSON array to extract hotspot information(The IIE,2023)
            for (i in 0 until jsonArray.length()) {
                //Getting the JSON object at the current index(The IIE,2023)
                val jsonObject = jsonArray.getJSONObject(i)

                //Extracting the attributes of the hotspot from the JSON object(The IIE,2023)
                val hotspotId = jsonObject.getString("locId")
                val hotspotName = jsonObject.getString("locName")
                val latitude = jsonObject.getDouble("lat")
                val longitude = jsonObject.getDouble("lng")

                //Creating a Hotspot object with the extracted data and add it to the list(The IIE,2023)
                hotspots.add(Hotspot(hotspotId, hotspotName, latitude, longitude))
            }
        } catch (e: JSONException) {
            //Handling any JSON parsing exceptions by printing the stack trace(The IIE,2023)
            e.printStackTrace()
        }
        return hotspots
    }

    //Function to initiate navigation to a selected hotspot(Google Maps Platform,2023)
    private fun startNavigationToHotspot(hotspotLatLng: LatLng) {
        //Checking if the app has the required location permission(Google Maps Platform,2023)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //If permission is granted, access the last known location(Google Maps Platform,2023)
            fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    //Retrieving the current location from the task result(Google Maps Platform,2023)
                    val currentLocation = task.result

                    //Creating a formatted origin and destination for the directions request(Google Maps Platform,2023)
                    val origin = "${currentLocation.latitude},${currentLocation.longitude}"
                    val destination = "${hotspotLatLng.latitude},${hotspotLatLng.longitude}"

                    //Constructing the URL for the Google Directions API request(Google Maps Platform,2023)
                    val directionsApiUrl = "https://maps.googleapis.com/maps/api/directions/json" +
                            "?origin=$origin" +
                            "&destination=$destination" +
                            "&key=AIzaSyANSfWGSCuajYhq8E3Ii46-pJSqlBsBFYA"

                    //Starting a new thread to perform the directions request(Google Maps Platform,2023)
                    Thread {
                        try {
                            val url = URL(directionsApiUrl)
                            val connection = url.openConnection() as HttpURLConnection
                            connection.requestMethod = "GET"
                            val responseCode = connection.responseCode

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                //Reading the response data from the input stream and store it as a string(Google Maps Platform,2023)
                                val response =
                                    connection.inputStream.bufferedReader().use { it.readText() }

                                //Parsing the directions response into a list of route points(Google Maps Platform,2023)
                                val routePoints = parseDirectionsResponse(response)

                                //Switching to the UI thread to draw the route on the map(Google Maps Platform,2023)
                                requireActivity().runOnUiThread {
                                    drawRouteOnMap(routePoints)
                                }
                            } else {
                                //Handling error response from the directions API(Google Maps Platform,2023)
                            }
                        } catch (e: Exception) {
                            //Handling exceptions that may occur during the network request(The IIE,2023)
                            e.printStackTrace()
                        }
                    }.start()
                }
            }
        } else {
            //Requesting the necessary location permission if it is not granted(The IIE,2023)
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_FINE_LOCATION
            )
        }
    }

    //Function to parse directions response data from Google Directions API and extract a list of route points (LatLng) for drawing the route on the map(Google Maps Platform,2023)
    private fun parseDirectionsResponse(response: String): List<LatLng> {
        //Creating an empty list to store the parsed route points(Google Maps Platform,2023)
        val routePoints = mutableListOf<LatLng>()

        try {
            //Parsing the JSON response string into a JSONObject(Google Maps Platform,2023)
            val jsonResponse = JSONObject(response)

            //Extracting the "routes" array from the JSON response(Google Maps Platform,2023)
            val routes = jsonResponse.getJSONArray("routes")

            //Checking if at least one route is present in the "routes" array(Google Maps Platform,2023)
            if (routes.length() > 0) {
                //Extracting the first "legs" array from the first route(Google Maps Platform,2023)
                val legs = routes.getJSONObject(0).getJSONArray("legs")

                //Extracting the "steps" array from the first leg(Google Maps Platform,2023)
                val steps = legs.getJSONObject(0).getJSONArray("steps")

                //Iterating through the "steps" array to extract polyline points(Google Maps Platform,2023)
                for (i in 0 until steps.length()) {
                    val step = steps.getJSONObject(i)

                    //Extracting the "polyline" object from the step(Google Maps Platform,2023)
                    val polyline = step.getJSONObject("polyline")

                    //Extracting the "points" string from the polyline(Google Maps Platform,2023)
                    val points = polyline.getString("points")

                    //Decoding the polyline points into a list of LatLng objects(Google Maps Platform,2023)
                    val decodedPoints = decodePolyline(points)

                    //Adding the decoded points to the routePoints list(Google Maps Platform,2023)
                    routePoints.addAll(decodedPoints)
                }
            }
        } catch (e: JSONException) {
            //Handling any JSON parsing exceptions by printing the stack trace(The IIE,2023)
            e.printStackTrace()
        }
        return routePoints
    }

    //Function to decode a polyline encoded string into a list of LatLng objects(Google Maps Platform,2023)
    private fun decodePolyline(encoded: String): List<LatLng> {
        //Creating an empty list to store the decoded LatLng points(Google Maps Platform,2023)
        val poly = ArrayList<LatLng>()

        //Initializing variables for loop control and latitude/longitude values(Google Maps Platform,2023)
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        // Looping through the encoded string to decode the polyline(Google Maps Platform,2023)
        while (index < len) {
            //Initializing variables for decoding(Google Maps Platform,2023)
            var b: Int
            var shift = 0
            var result = 0

            //Decoding latitude value(Google Maps Platform,2023)
            do {
                //Extracting the next character from the encoded string and convert it to an integer(Google Maps Platform,2023)
                b = encoded[index++].toInt() - 63
                //Updating the result by bitwise OR with the extracted value and shifting it by the appropriate amount(Google Maps Platform,2023)
                result = result or (b and 0x1F shl shift)
                shift += 5
            } while (b >= 0x20)

            //Calculating the delta latitude based on whether it's positive or negative(Google Maps Platform,2023)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            //Resetting variables for decoding longitude(Google Maps Platform,2023)
            shift = 0
            result = 0

            //Decoding longitude value(Google Maps Platform,2023)
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1F shl shift)
                shift += 5
            } while (b >= 0x20)

            //Calculating the delta longitude based on whether it's positive or negative(Google Maps Platform,2023)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            //Creating a LatLng object using the decoded latitude and longitude values and add it to the list(Google Maps Platform,2023)
            val latLng = LatLng(lat / 1E5, lng / 1E5)
            poly.add(latLng)
        }
        return poly
    }


    //Function to draw a route on the map using a list of LatLng points(Google Maps Platform,2023)
    private fun drawRouteOnMap(routePoints: List<LatLng>) {
        //Creating a PolylineOptions object to configure the appearance of the route(Google Maps Platform,2023)
        val polylineOptions = PolylineOptions()
            .addAll(routePoints)  //Adding all LatLng points to the polyline(Google Maps Platform,2023)
            .color(Color.BLUE)    //Setting the color of the polyline to blue(Google Maps Platform,2023)
            .width(8f)            //Setting the width of the polyline to 8 pixels(Google Maps Platform,2023)

        //Adding the polyline to the Google Map(Google Maps Platform,2023)
        googleMap.addPolyline(polylineOptions)
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

        //Request code for fine location permission. This code is used when requesting permission to access fine location(The IIE,2023)
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
