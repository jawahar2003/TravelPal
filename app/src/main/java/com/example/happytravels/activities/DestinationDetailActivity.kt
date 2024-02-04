package com.example.happytravels.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.happytravels.R
import com.example.happytravels.databinding.ActivityDestinationDetailBinding
import com.example.happytravels.models.TravelDestinationModel
import kotlinx.android.synthetic.main.activity_destination_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.RoundingMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DestinationDetailActivity : AppCompatActivity(){
    val SMS_PERMISSION_CODE = 123
    val CHANNEL_ID ="ALARM"
    val CHANNEL_NAME="ALARM"
    private var _binding: ActivityDestinationDetailBinding? = null
    private val binding: ActivityDestinationDetailBinding
        get() = _binding!!

    private var service: Intent? = null
    private val backgroundLocation =  registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {

        }
    }

   @RequiresApi(Build.VERSION_CODES.N)
   private val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
       when {
           it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,false)->{

               if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                   if (ActivityCompat.checkSelfPermission(
                           this,
                           android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                   )!= PackageManager.PERMISSION_GRANTED
                   ){
                        backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                   }
               }

           }
           it.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,false)->{

           }
       }
   }

    var destinationPlaceDetailModel : TravelDestinationModel? = null
    var cLatitude: Double? = null
    var cLongitude: Double? = null
    lateinit var notification: Notification
    lateinit var notificationManager: NotificationManagerCompat


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        /*setContentView(R.layout.activity_destination_detail)*/
        _binding = ActivityDestinationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.SEND_SMS),
                SMS_PERMISSION_CODE
            )
        } else {
            // Permission is already granted, do nothing
        }

        service = Intent(this,LocationService::class.java)

        binding.apply {
            btn_view_startService.setOnClickListener {
                checkPermissions()
            }

            btn_view_stopService.setOnClickListener {
                stopService(service)
                remaining_km.text = "~"

            }
        }





         if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
             destinationPlaceDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as TravelDestinationModel?
         }

        if(destinationPlaceDetailModel != null){
            setSupportActionBar(toolbar_destination_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = destinationPlaceDetailModel!!.title

            toolbar_destination_detail.setNavigationOnClickListener {
                onBackPressed()
            }

            tv_location.text = destinationPlaceDetailModel!!.location

            btn_view_on_map.setOnClickListener{
                val intent = Intent(this,MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, destinationPlaceDetailModel)
                startActivity(intent)
            }
        }
        createNotificationChannel()
        val intent = Intent(this, Alarm::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run{
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
        }
       /* val uri = Uri.parse("android.resource://" + Context.getPackageName() + "/raw/mysound.mp3");*/
        notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Destination Reached")
            .setContentText("")
            /*.setSound(Notification.DEFAULT_SOUND)*/
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager = NotificationManagerCompat.from(this)


    }
    override fun onStart(){
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissions.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }else{
                startService(service)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        stopService(service)
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent){
        /*remaining_km.text= "${locationEvent.latitude}"*/
/*       rLongitude = "Longitude -> ${locationEvent.longitude}"*/
        cLatitude = locationEvent.latitude
        cLongitude = locationEvent.longitude
        var distance = calculateDistance(cLatitude!!,cLongitude!!,destinationPlaceDetailModel!!.latitude,destinationPlaceDetailModel!!.longitude)
        remaining_km.text = "$distance Km"
        if (distance < 5){

            notificationManager.notify(0,notification)
        }
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        return distance.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }
    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID,CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH).apply {
                lightColor = Color.GREEN
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    fun isScreenOn(activity: Activity): Boolean {
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            @Suppress("DEPRECATION")
            powerManager.isScreenOn
        }
    }
}