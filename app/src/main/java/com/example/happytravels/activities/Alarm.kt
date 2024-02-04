package com.example.happytravels.activities

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.happytravels.R
import java.io.IOException
import java.lang.Math.abs

class Alarm : AppCompatActivity(), GestureDetector.OnGestureListener {



    private lateinit var gestureDetector: GestureDetector
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        playAudio()
        showGIF()

        gestureDetector = GestureDetector(this, this)

    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1.y < e2.y) {
            // swipe from top to bottom
            return false
        } else if (e1.y > e2.y) {
            // swipe from bottom to top
            Toast.makeText(this, "Swiped from bottom to top", Toast.LENGTH_SHORT).show()
            pauseAudio()
            finish()
            return true
        }
        return false
    }

    // other gesture detection callbacks that we don't need to implement
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean { return false }
    override fun onDown(e: MotionEvent): Boolean { return false }
    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean { return false }
    override fun onLongPress(e: MotionEvent) {}

    private fun playAudio(){

       mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
       mediaPlayer.setLooping(true)
       mediaPlayer.start()

        Toast.makeText(this,"Audio started playing",Toast.LENGTH_SHORT).show()
    }

    private fun pauseAudio(){
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        }
        else{
            Toast.makeText(this,"Audio has not played",Toast.LENGTH_SHORT).show()
        }

    }

    fun showGIF(){
        val imageView: ImageView = findViewById(R.id.imageView2)
        Glide.with(this).load(R.raw.duck_walk).into(imageView)
    }


}