package com.example.happytravels.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.happytravels.R
import com.example.happytravels.database.DatabaseHandler
import com.example.happytravels.models.TravelDestinationModel
import com.example.happytravels.utils.GetAddressFromLatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_add_destination_places.*

class AddDestinationPlacesActivity : AppCompatActivity(), View.OnClickListener{


    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mDestinationPlaceDetails: TravelDestinationModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_destination_places)
        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }


        if(!Places.isInitialized()){
            Places.initialize(this@AddDestinationPlacesActivity,
                resources.getString(R.string.YOUR_API_KEY))
        }

    if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
        mDestinationPlaceDetails = intent.getParcelableExtra(
            MainActivity.EXTRA_PLACE_DETAILS) as TravelDestinationModel?
    }
        if(mDestinationPlaceDetails != null){
            supportActionBar?.title ="Edit Destination Place"
            et_title.setText(mDestinationPlaceDetails!!.title)
            et_location.setText(mDestinationPlaceDetails!!.location)
            mLatitude = mDestinationPlaceDetails!!.latitude
            mLongitude = mDestinationPlaceDetails!!.longitude

            btn_save.text = "UPDATE"
        }
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_mark_on_map.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btn_save -> {
                when {
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter Title ",Toast.LENGTH_LONG).show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please select a Location",Toast.LENGTH_LONG).show()
                    }else ->{
                        val travelDestinationModel = TravelDestinationModel(
                            if(mDestinationPlaceDetails == null) 0 else mDestinationPlaceDetails!!.id,
                            et_title.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                    val dbHandler = DatabaseHandler(this)

                    if(mDestinationPlaceDetails == null){
                        val addDestination = dbHandler.addDestination(travelDestinationModel)

                        if(addDestination>0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }else{
                        val updateDestination = dbHandler.updateDestination(travelDestinationModel)

                        if(updateDestination>0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                    }


                    }

                }

            }
            R.id.et_location ->{
                try {
                    val fields = listOf(
                        Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                        .build(this@AddDestinationPlacesActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            R.id.tv_mark_on_map ->{
                val intent = Intent(this,MarkOnMapActivity::class.java)
                startActivityForResult(intent,REQUEST_GET_MARKER_LOCATION)
            }

        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            et_location.setText(place.address)
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
        }else if (requestCode == REQUEST_GET_MARKER_LOCATION && resultCode == Activity.RESULT_OK){
            mLatitude = data!!.getDoubleExtra("Latitude",0.0)
            mLongitude = data!!.getDoubleExtra("Longitude",0.0)
            Log.e("Current Latitude","$mLatitude")
            Log.e("Current Longitude","$mLongitude")

        val addressTask = GetAddressFromLatLng(this, mLatitude,mLongitude)
        addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener{
               override fun onAddressFound(address: String?){
                    et_location.setText(address)
               }
                override fun onError(){
                    Log.e("Get Address:: ", "Something went wrong")
                }
            })
            addressTask.getAddress()

        }


    }

    companion object{
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        private const val REQUEST_GET_MARKER_LOCATION = 4
    }
}