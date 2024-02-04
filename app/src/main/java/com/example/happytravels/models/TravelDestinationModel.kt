package com.example.happytravels.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class TravelDestinationModel(
  val id: Int,
  val title: String?,
  val location: String?,
  val latitude: Double,
  val longitude: Double
 ): Parcelable {
  constructor(parcel: Parcel) : this(
    parcel.readInt(),
    parcel.readString(),
    parcel.readString(),
    parcel.readDouble(),
    parcel.readDouble()
  ) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeInt(id)
    parcel.writeString(title)
    parcel.writeString(location)
    parcel.writeDouble(latitude)
    parcel.writeDouble(longitude)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<TravelDestinationModel> {
    override fun createFromParcel(parcel: Parcel): TravelDestinationModel {
      return TravelDestinationModel(parcel)
    }

    override fun newArray(size: Int): Array<TravelDestinationModel?> {
      return arrayOfNulls(size)
    }
  }
}