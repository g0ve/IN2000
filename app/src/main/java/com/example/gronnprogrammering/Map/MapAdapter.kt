@file:Suppress("DEPRECATION")

package com.example.gronnprogrammering.Map

import android.content.Context
import android.preference.PreferenceManager

import android.util.Log
import com.example.gronnprogrammering.DTO.MyLocationDTO
import com.example.gronnprogrammering.DTO.StationDTO
import com.example.gronnprogrammering.GetCalls
import com.example.gronnprogrammering.RetrofitClientInstance
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import com.example.gronnprogrammering.R
import com.mapbox.mapboxsdk.annotations.MarkerOptions


class MapAdapter(val elements : List<StationDTO>, val mapboxMap: MapboxMap, val context: Context) {
    var listAqi = arrayListOf<String>()
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun adapt() : List<String>{
        for (station: StationDTO in elements) {
            callStation(station)
        }
        return listAqi
    }

    /*
    This is where we attach an AQI-value to the different stations by doing another retrofit-request to API with
    the station-coordinates as parameters.
    */
    private fun callStation(station: StationDTO) {
        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllMyLocation(station.latitude, station.longitude)

        call?.enqueue(object : Callback<MyLocationDTO> {
            override fun onFailure(call: Call<MyLocationDTO>, t: Throwable) {
            }

            override fun onResponse(call: Call<MyLocationDTO>, response: Response<MyLocationDTO>) {


                val myLocationList = response.body()!!//listen

                getCurrentTime()
                var twoDesimalAqi = ""
                var aqi:Double = 0.0
                for (time in myLocationList.data.time) {
                    val stringTime: String = getCurrentTime()
                    if (time.to == stringTime) {
                        aqi = time.variables.AQI.value
                        twoDesimalAqi = """${(Math.round(aqi * 100.0) / 100.0)} AQI"""
                    }
                }

                /*
                Creates markers with the color scale for colorblind people
                */
                if(prefs.getBoolean("colorblind", false)) {
                    var icon = IconFactory.getInstance(context).fromResource(R.drawable.colorblind_cloud_low)

                    if (aqi >= 4) {
                        icon = IconFactory.getInstance(context).fromResource(R.drawable.colorblind_cloud_veryhigh)
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    }

                    if (aqi >= 3 && aqi < 4) {
                        icon = IconFactory.getInstance(context).fromResource(R.drawable.colorblind_cloud_high)
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    } else if (aqi >= 2 && aqi < 3) {
                        icon = IconFactory.getInstance(context).fromResource(R.drawable.colorblind_cloud_moderate)
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    } else {
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    }
                }

                /*
                Creates markers with the standard color codes.
                */
                else{
                    var icon = IconFactory.getInstance(context).fromResource(R.drawable.cloud_low)
                    if (aqi >= 4) {
                        icon = IconFactory.getInstance(context).fromResource(R.drawable.cloud_veryhigh)
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    }
                    if (aqi >= 3 && aqi < 4) {
                        icon = IconFactory.getInstance(context).fromResource(R.drawable.cloud_high)
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    } else if (aqi >= 2 && aqi < 3) {
                        icon = IconFactory.getInstance(context).fromResource(R.drawable.cloud_moderate)
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    } else {
                        mapboxMap.addMarker(
                            MarkerOptions().setTitle(station.name).setSnippet(twoDesimalAqi)
                                .position(LatLng(station.latitude.toDouble(), station.longitude.toDouble())).setIcon(
                                    icon
                                )
                        )
                    }
                }
            }
        })
    }

    /*
    Retrieving the time from the device, this is used to find the current time of the user such that the user gets
    correct AQI values.
    */
    fun getCurrentTime(): String{
        val calendar: Calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH':00:00Z'")
        Log.d("CodeAndroidLocation", "Tiden nå er: " + timeFormat.format(calendar.time))
        return timeFormat.format(calendar.time)
    }
}