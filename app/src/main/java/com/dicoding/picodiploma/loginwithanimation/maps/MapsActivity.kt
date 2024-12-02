package com.dicoding.picodiploma.loginwithanimation.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.database.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMapsBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lifecycleScope.launch {
            UserPreference.getInstance(dataStore).getSession().collect { user ->
                val token = user.token
                if (token.isNotEmpty()) {
                    initializeViewModel(token)
                }
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mapsViewModel.getStoriesLocation()
        mapsViewModel.stories.observe(this) { stories ->
            for (story in stories) {
                val lat = story.lat
                val lon = story.lon

                if (lat != null && lon != null) {
                    val location = LatLng(lat, lon)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(story.name)
                            .snippet(story.description)
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
                }
            }
        }
    }

    private fun initializeViewModel(token: String) {
        mapsViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                UserRepository.getInstance(

                    ApiConfig.getApiService(token),
                    UserPreference.getInstance(dataStore),
                            StoryDatabase.getDatabase(applicationContext)

                )
            )
        )[MapsViewModel::class.java]
    }
}
