package com.example.happytravels.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happytravels.R
import com.example.happytravels.adapters.DestinationPlacesAdapter
import com.example.happytravels.database.DatabaseHandler
import com.example.happytravels.models.TravelDestinationModel
import com.example.happytravels.utils.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.activity_main.*
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


      fabAddDestination.setOnClickListener{
        val intent = Intent(this, AddDestinationPlacesActivity::class.java)
        startActivityForResult(intent,ADD_PLACE_ACTIVITY_REQUEST_CODE)

      }
        getDestinationListFromLocalDB()
    }

    private fun setupDestinationPlacesRecyclerView(destinationPlaceList: ArrayList<TravelDestinationModel>){
        rv_Destination_places_list.layoutManager = LinearLayoutManager(this)
        rv_Destination_places_list.setHasFixedSize(true)
        val placesAdapter = DestinationPlacesAdapter(this,destinationPlaceList)
        rv_Destination_places_list.adapter = placesAdapter


        placesAdapter.setOnClickListener(object : DestinationPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: TravelDestinationModel) {
                val intent = Intent(this@MainActivity, DestinationDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_Destination_places_list.adapter as DestinationPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_Destination_places_list)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_Destination_places_list.adapter as DestinationPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getDestinationListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_Destination_places_list)
    }


    private fun getDestinationListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getDestinationPlaceList : ArrayList<TravelDestinationModel> = dbHandler.getDestinationPlacesList()

        if(getDestinationPlaceList.size > 0 ){
            rv_Destination_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setupDestinationPlacesRecyclerView(getDestinationPlaceList)
        }
        else{
            rv_Destination_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            getDestinationListFromLocalDB()
        }else{
            Log.e("Activity","cancelled or back pressed")
        }
    }
    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}