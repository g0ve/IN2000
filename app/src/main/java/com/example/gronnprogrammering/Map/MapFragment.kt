@file:Suppress("DEPRECATION")

package com.example.gronnprogrammering.Map

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.gronnprogrammering.DTO.StationDTO
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.gronnprogrammering.GetCalls
import com.example.gronnprogrammering.R
import com.example.gronnprogrammering.RetrofitClientInstance

@Suppress("DEPRECATION")
class MapFragment : Fragment() {


    private lateinit var mapView: MapView

    // We create a global stationlist where we use the data to parse to MapAdapter-class
    var stationlist = arrayListOf<StationDTO>()
    var colorblind: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        colorblind = prefs.getBoolean("colorblind", false)

        val context = container!!.context
        // Getting an instance of Mapbox with the use of a key retrieved from a single Mapbox-user
        Mapbox.getInstance(this.activity!!, getString(R.string.access_token))


        // The set-up layout for the standard map
        var view = inflater.inflate(R.layout.fragment_map, container, false)
        // The set-up for a colorblind version of the map
        if(colorblind) {
            view = inflater.inflate(R.layout.colorblind_activity_map, container, false)
        }

        mapView = view?.findViewById<MapView?>(R.id.mapView)!!
        mapView.onCreate(savedInstanceState)


        // Fetching station-data from the API. Done in the same fashion as in the other fragments
        val service = RetrofitClientInstance.retrofitInstanse!!.create(GetCalls::class.java)
        val call = service.getAllStations()

        call.enqueue(object : Callback<List<StationDTO>> {
            // If the connectivity to the API fails this message will pop up
            override fun onFailure(call: Call<List<StationDTO>>, t: Throwable) {
                Toast.makeText(activity, "Det oppstod en feil, prøv igjen senere", Toast.LENGTH_LONG).show()
            }

            // If the connection is successful the following will happen
            override fun onResponse(call: Call<List<StationDTO>>, response: Response<List<StationDTO>>) {
                // Since the API-call will return a List, we use the data to create a temporary list which later gets
                // used in a loop.
                val temporarylist = response.body()!!
                for (station:StationDTO in temporarylist) {
                    stationlist.add(station)

                }

                /*
                If the colorblind-mode is checked off we create the map in a different style which is more adapted
                for colorblind users.
                */
                if(colorblind) {
                    mapView.getMapAsync { mapboxMap ->
                        // sets the map to have the wanted map-style/layout
                        mapboxMap.setStyle(Style.LIGHT) {
                        }

                        /*
                        Here is where we send the stationlist, mapbox-instance and the fragment context to the
                        MapAdapter-class. This needs to be done because each instance of mapbox runs on different
                        threads. If we were to run them both in the same class, it will result in a NullPointer-error.
                        Therefore we decided to set markerers in the MapAdapter-class.
                        */
                        val adapter = MapAdapter(stationlist, mapboxMap, context)
                        adapter.adapt()
                    }
                }
                // This runs in the same way as above, except that it uses a different style.
                else {
                    mapView.getMapAsync { mapboxMap ->
                        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                        }

                        val adapter = MapAdapter(stationlist, mapboxMap, context)
                        adapter.adapt()
                    }
                }

            }
        })

        return view
    }

    // Life cycle methods, neccessary for Mapbox to run
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    companion object {
        fun newInstance(): MapFragment = MapFragment()
    }

}
