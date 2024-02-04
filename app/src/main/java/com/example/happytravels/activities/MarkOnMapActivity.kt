package com.example.happytravels.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.happytravels.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.example.happytravels.databinding.ActivityMarkOnMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_mark_on_map.*
import java.util.*

class MarkOnMapActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {
    var currentMarker: Marker? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMarkOnMapBinding
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var currentLocation: Location? = null
    var lat:Double? = null
    var lon:Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMarkOnMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar_map2)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "DRAG TO MARK"

        toolbar_map2.setNavigationOnClickListener {
            onBackPressed()
        }




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
       /* val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
        fabAddDestination.setOnClickListener(this)
    }



    private fun fetchLocation(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION),1000)
            return
        }
        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener { location ->
            if(location != null){
                this.currentLocation = location
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map2) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1000 -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fetchLocation()
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        val latlong = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
        drawMarker(latlong)

        mMap.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener{
            override fun onMarkerDrag(p0: Marker) {

            }

            override fun onMarkerDragEnd(p0: Marker) {
                if (currentMarker != null)
                    currentMarker?.remove()

                val newLatLng = LatLng(p0.position.latitude, p0.position.longitude)
                drawMarker(newLatLng)

            }

            override fun onMarkerDragStart(p0: Marker) {

            }
        })

    }
     private fun drawMarker(latlong: LatLng){
        val markerOption = MarkerOptions().position(latlong).title("Your destination")
            .snippet(getAddress(latlong.latitude, latlong.longitude)).draggable(true)

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlong))
        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(latlong,14f)))
        currentMarker = mMap.addMarker(markerOption)
        currentMarker?.showInfoWindow()
        lat = latlong.latitude
        lon = latlong.longitude
        Toast.makeText(this, "Lat: $lat, Lon: $lon", Toast.LENGTH_LONG).show()

    }

    private fun getAddress(lat: Double, lon: Double): String {
        val geoCoder =  Geocoder(this, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(lat,lon,1)
        return addresses?.get(0)?.getAddressLine(0).toString()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.fabAddDestination ->{

                val intent = Intent(this,AddDestinationPlacesActivity::class.java)
                intent.putExtra("Latitude",lat)
                intent.putExtra("Longitude",lon)
                setResult(Activity.RESULT_OK,intent)
                finish()

            }
        }
    }
}