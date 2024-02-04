package com.example.happytravels.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happytravels.R
import com.example.happytravels.activities.AddDestinationPlacesActivity
import com.example.happytravels.activities.MainActivity
import com.example.happytravels.database.DatabaseHandler
import com.example.happytravels.models.TravelDestinationModel
import kotlinx.android.synthetic.main.item_destination_place.view.*

open class DestinationPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<TravelDestinationModel>
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return MyViewHolder(
           LayoutInflater.from(context).inflate(
               R.layout.item_destination_place,
               parent,
               false
           )
       )
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.itemView.tvTitle.text = model.title


            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }
fun removeAt(position: Int){
    val dbHandler = DatabaseHandler(context)
    val isDeleted = dbHandler.deleteDestinationPlace(list[position])
    if(isDeleted > 0 ){
        list.removeAt(position)
        notifyItemRemoved(position)
    }
}
fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
    val intent = Intent(context, AddDestinationPlacesActivity::class.java)
    intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
    activity.startActivityForResult(intent, requestCode)
    notifyItemChanged(position)
}

    override fun getItemCount(): Int {
        return list.size
    }


    interface OnClickListener {
        fun onClick(position: Int, model: TravelDestinationModel)
    }



    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}