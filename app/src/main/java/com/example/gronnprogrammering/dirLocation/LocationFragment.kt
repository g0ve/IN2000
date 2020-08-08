package com.example.gronnprogrammering.dirLocation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.gronnprogrammering.DTO.MyLocationDTO
import com.example.gronnprogrammering.GetCalls
import com.example.gronnprogrammering.R
import com.example.gronnprogrammering.RetrofitClientInstance
import kotlinx.android.synthetic.main.fragment_location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class LocationFragment : Fragment() {

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    var globalLongitude: Double = 0.0
    var globalLatitude: Double = 0.0
    var locationName: String = ""
    var areaName: String = ""
    var colorblind: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_location, container, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        colorblind = prefs.getBoolean("colorblind", false)

        //Sets onclick listner for info button (contains info about AQI and pollution
        val infoclick = view?.findViewById(R.id.info_click) as TextView
        infoclick.setOnClickListener {
            //Checks if colorblind mode is enabled
            if(colorblind) {
                val dialogView = LayoutInflater.from(activity).inflate(R.layout.info_substances_colorblindpoppop, null)
                val info_dialog = AlertDialog.Builder(dialogView.context)
                    .setView(dialogView)
                info_dialog.show()
            }
            else{
                val dialogView = LayoutInflater.from(activity).inflate(R.layout.info_substances_poppop, null)
                val info_dialog = AlertDialog.Builder(dialogView.context)
                    .setView(dialogView)
                info_dialog.show()
            }
        }

        //Starting with getting the users loaction
        getLocation()
        //When location is acquired, du a retrofit call on API and get info
        callMyLocation()

        return view
    }

    //Static method - Make a new instance of the fragment
    companion object {
        fun newInstance(): LocationFragment =
            LocationFragment()
    }

    //Own method for inserting all info from API in right views
    private fun setValues(myLocationList:MyLocationDTO){
        val tvAQI = view?.findViewById<TextView>(R.id.tvAQI)
        val tvPm10 = view?.findViewById<TextView>(R.id.tvPm10)
        val tvPm25 = view?.findViewById<TextView>(R.id.tvPm25)
        val tvNo2 = view?.findViewById<TextView>(R.id.tvNo2)
        val tvO3 = view?.findViewById<TextView>(R.id.tvO3)

        //text info and click items
        val imgViewArray = arrayListOf<ImageView?>()

        val imgclick1 = view?.findViewById<ImageView>(R.id.imgAir)
        val imgclick2 = view?.findViewById<ImageView>(R.id.imgRunning)
        val imgclick3 = view?.findViewById<ImageView>(R.id.imgMask)
        imgViewArray.add(imgclick1)
        imgViewArray.add(imgclick2)
        imgViewArray.add(imgclick3)
        val textBox = view?.findViewById<TextView>(R.id.info_shortTextView)

        var windowBorder: Int
        var excerciseBorder: Int
        var textBorder: Int

        var textWindow: String
        var textExercise: String
        var text: String

        val stringTime:String = getCurrentTime()
        for(time in myLocationList.data.time) {
            if(time.to == stringTime){
                val aqiLevel = time.variables.AQI.value
                val twoDesimalAqi = Math.round(aqiLevel *100.0)/100.0
                val pollutionPm10 = time.variables.pm10_concentration.value.roundToInt()
                val pollutionPm25 = time.variables.pm25_concentration.value.roundToInt()
                val pollutionNo2 = time.variables.no2_concentration.value.roundToInt()
                val pollutionO3 = time.variables.o3_concentration.value.roundToInt()


                ///Fargeblindoppsett
                if(colorblind){
                    when {
                        aqiLevel >=4 -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_colorblindveryhigh)

                            windowBorder = R.drawable.textviewshadow_colorblindveryhigh
                            excerciseBorder = R.drawable.textviewshadow_colorblindveryhigh
                            textBorder = R.drawable.textviewshadow_colorblindhigh

                            for(item in imgViewArray){
                                item?.setBackgroundResource(R.drawable.textviewshadow_colorblindveryhigh)
                            }
                            textWindow = "Luften ute er svært dårlig, lukk alle luker"
                            textExercise = "Utendørsaktivitet er ikke anbefalt"
                            text = "Bruk av luftmaske utendørs er anbefalt"

                        }
                        aqiLevel in 3..3 -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_colorblindhigh)

                            windowBorder = R.drawable.textviewshadow_colorblindhigh
                            excerciseBorder = R.drawable.textviewshadow_colorblindhigh
                            textBorder = R.drawable.textviewshadow_colorblindmoderate

                            for(item in imgViewArray){
                                item?.setBackgroundResource(R.drawable.textviewshadow_colorblindhigh)
                            }
                            textWindow = "Alle burde holde vinduene lukket"
                            textExercise = "Redusert utendørsaktivitet anbefales"
                            text = "Det kan være lurt med bruk av luftmaske utendørs"
                        }
                        aqiLevel in 2..2 -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_colorblindmoderate)

                            windowBorder = R.drawable.textviewshadow_colorblindmoderate
                            excerciseBorder = R.drawable.textviewshadow_colorblindmoderate
                            textBorder = R.drawable.textviewshadow_colorblindlow

                            for(item in imgViewArray){
                                if(item == imgclick3){
                                    item?.setBackgroundResource(R.drawable.textviewshadow_colorblindlow)
                                }
                                else{
                                    item?.setBackgroundResource(R.drawable.textviewshadow_colorblindmoderate)
                                }
                            }
                            textWindow = "Utsatte grupper burde holde vinduene lukket"
                            textExercise = "Nyt utendørsaktivitet, men spessielt utsatte grupper burde ha redusert utetid"
                            text = "Det er ikke nødvendig med bruk av luftmaske"
                        }
                        else -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_colorblindlow)

                            windowBorder = R.drawable.textviewshadow_colorblindlow
                            excerciseBorder = R.drawable.textviewshadow_colorblindlow
                            textBorder = R.drawable.textviewshadow_colorblindlow

                            for(item in imgViewArray){
                                item?.setBackgroundResource(R.drawable.textviewshadow_colorblindlow)
                            }
                            textWindow = "Luften er ren, hold vinduene åpne"
                            textExercise = "Nyt utendørsaktivitet"
                            text = "Det er ikke nødvendig med bruk av luftmaske"
                        }
                    }

                    //Pollution background for pm10
                    when {
                        pollutionPm10>= 150 -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_colorblindveryhigh)
                        pollutionPm10 in 50..149 -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_colorblindhigh)
                        pollutionPm10 in 30..49 -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_colorblindmoderate)
                        else -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_colorblindlow)
                    }

                    //Pollution background for pm25
                    when {
                        pollutionPm25>= 75 -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_colorblindveryhigh)
                        pollutionPm25 in 25..74 -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_colorblindhigh)
                        pollutionPm25 in 15..24 -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_colorblindmoderate)
                        else -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_colorblindlow)
                    }

                    //Pollution background for No2
                    when {
                        pollutionNo2>= 400 -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_colorblindveryhigh)
                        pollutionNo2 in 200..399 -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_colorblindhigh)
                        pollutionNo2 in 100..199 -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_colorblindmoderate)
                        else -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_colorblindlow)
                    }

                    //Pollution background for O3
                    when {
                        pollutionO3>= 240 -> tvO3?.setBackgroundResource(R.drawable.textview_circel_colorblindveryhigh)
                        pollutionO3 in 180..239 -> tvO3?.setBackgroundResource(R.drawable.textview_circel_colorblindhigh)
                        pollutionO3 in 100..179 -> tvO3?.setBackgroundResource(R.drawable.textview_circel_colorblindmoderate)
                        else -> tvO3?.setBackgroundResource(R.drawable.textview_circel_colorblindlow)
                    }
                }

                //vanlig oppsett
                else {

                    when {
                        aqiLevel >= 4 -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_veryhigh)

                            windowBorder = R.drawable.textviewshadow_veryhigh
                            excerciseBorder = R.drawable.textviewshadow_veryhigh
                            textBorder = R.drawable.textviewshadow_high

                            for (item in imgViewArray) {
                                item?.setBackgroundResource(R.drawable.textviewshadow_veryhigh)
                            }
                            textWindow = "Luften ute er svært dårlig, lukk alle luker"
                            textExercise = "Utendørsaktivitet er ikke anbefalt"
                            text = "Bruk av luftmaske utendørs er anbefalt"

                        }
                        aqiLevel in 3..3 -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_high)

                            windowBorder = R.drawable.textviewshadow_high
                            excerciseBorder = R.drawable.textviewshadow_high
                            textBorder = R.drawable.textviewshadow_moderate

                            for (item in imgViewArray) {
                                item?.setBackgroundResource(R.drawable.textviewshadow_high)
                            }
                            textWindow = "Alle burde holde vinduene lukket"
                            textExercise = "Redusert utendørsaktivitet anbefales"
                            text = "Det kan være lurt med bruk av luftmaske utendørs"
                        }
                        aqiLevel in 2..2 -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_moderate)

                            windowBorder = R.drawable.textviewshadow_moderate
                            excerciseBorder = R.drawable.textviewshadow_moderate
                            textBorder = R.drawable.textviewshadow_low

                            for (item in imgViewArray) {
                                if (item == imgclick3) {
                                    item?.setBackgroundResource(R.drawable.textviewshadow_low)
                                } else {
                                    item?.setBackgroundResource(R.drawable.textviewshadow_moderate)
                                }
                            }
                            textWindow = "Utsatte grupper burde holde vinduene lukket"
                            textExercise =
                                "Nyt utendørsaktivitet, men spessielt utsatte grupper burde ha redusert utetid"
                            text = "Det er ikke nødvendig med bruk av luftmaske"
                        }
                        else -> {
                            ivCloud?.setImageResource(R.drawable.ic_cloud_low)

                            windowBorder = R.drawable.textviewshadow_low
                            excerciseBorder = R.drawable.textviewshadow_low
                            textBorder = R.drawable.textviewshadow_low

                            for (item in imgViewArray) {
                                item?.setBackgroundResource(R.drawable.textviewshadow_low)
                            }
                            textWindow = "Luften er ren, hold vinduene åpne"
                            textExercise = "Nyt utendørsaktivitet"
                            text = "Det er ikke nødvendig med bruk av luftmaske"
                        }
                    }

                    //Pollution background for pm10
                    when {
                        pollutionPm10 >= 150 -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_veryhigh)
                        pollutionPm10 in 50..149 -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_high)
                        pollutionPm10 in 30..49 -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_moderate)
                        else -> tvPm10?.setBackgroundResource(R.drawable.textview_circel_low)
                    }

                    //Pollution background for pm25
                    when {
                        pollutionPm25 >= 75 -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_veryhigh)
                        pollutionPm25 in 25..74 -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_high)
                        pollutionPm25 in 15..24 -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_moderate)
                        else -> tvPm25?.setBackgroundResource(R.drawable.textview_circel_low)
                    }

                    //Pollution background for No2
                    when {
                        pollutionNo2 >= 400 -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_veryhigh)
                        pollutionNo2 in 200..399 -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_high)
                        pollutionNo2 in 100..199 -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_moderate)
                        else -> tvNo2?.setBackgroundResource(R.drawable.textview_circel_low)
                    }

                    //Pollution background for O3
                    when {
                        pollutionO3 >= 240 -> tvO3?.setBackgroundResource(R.drawable.textview_circel_veryhigh)
                        pollutionO3 in 180..239 -> tvO3?.setBackgroundResource(R.drawable.textview_circel_high)
                        pollutionO3 in 100..179 -> tvO3?.setBackgroundResource(R.drawable.textview_circel_moderate)
                        else -> tvO3?.setBackgroundResource(R.drawable.textview_circel_low)
                    }
                }
                tvAQI?.text = twoDesimalAqi.toString()
                tvPm10?.text = pollutionPm10.toString()
                tvPm25?.text = pollutionPm25.toString()
                tvNo2?.text = pollutionNo2.toString()
                tvO3?.text = pollutionO3.toString()

                imgclick1?.setOnClickListener {
                    textBox?.setBackgroundResource(windowBorder)
                    textBox?.text = textWindow
                }
                imgclick2?.setOnClickListener {
                    textBox?.setBackgroundResource(excerciseBorder)
                    textBox?.text = textExercise
                }
                imgclick3?.setOnClickListener {
                    textBox?.setBackgroundResource(textBorder)
                    textBox?.text = text
                }

            }
        }
    }

    //Gets the current time from user, and format it to match time format in API
    fun getCurrentTime(): String{
        val calendar: Calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH':00:00Z'")
        return timeFormat.format(calendar.time)
    }

    //Updates the global defined location name
    private fun updateLocationName(name:String){
        locationName = name
    }

    //Update textview with new location name and part area
    private fun updateTextView(){
        val tvw = view?.findViewById<TextView>(R.id.tvOmrad)
        val tvw2 = view?.findViewById<TextView>(R.id.tvBy)

        tvw?.text = locationName
        tvw2?.text = areaName
    }

    //Gets users location/coordinates with help from LocationManager and listner LocationListner
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {
            if (hasGps) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1000F, object :
                    LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            locationGps = location

                            globalLatitude = locationGps!!.latitude
                            globalLongitude = locationGps!!.longitude

                            callMyLocation()
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localLocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (localLocationGps != null) {
                    locationGps = localLocationGps
                    globalLatitude = locationGps!!.latitude
                    globalLongitude = locationGps!!.longitude
                }
            }
            if (hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1000F, object :
                    LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            locationNetwork = location

                            globalLatitude = locationNetwork!!.latitude
                            globalLongitude = locationNetwork!!.longitude

                            callMyLocation()
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localLocationNetwork != null) {
                    locationNetwork = localLocationNetwork

                    globalLatitude = locationNetwork!!.latitude
                    globalLongitude = locationNetwork!!.longitude
                }
            }

            if (locationGps != null && locationNetwork != null) {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                    LocationFragment().globalLatitude = locationNetwork!!.latitude
                    LocationFragment().globalLongitude = locationNetwork!!.longitude

                } else {
                    LocationFragment().globalLatitude = locationGps!!.latitude
                    LocationFragment().globalLongitude = locationGps!!.longitude

                }
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    //Does a retrofit call on API with coordinates has parameters. Then use all info from api response
    private fun callMyLocation(){
        //Bruker retrofit
        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllMyLocation(globalLatitude.toString(), globalLongitude.toString())

        call?.enqueue(object : Callback<MyLocationDTO> {

            //Dersom noe gikk galt under tilkobling av API
            override fun onFailure(call: Call<MyLocationDTO>, t: Throwable) {
            }

            //Dersom den har koblet til API suksessfult
            override fun onResponse(call: Call<MyLocationDTO>, response: Response<MyLocationDTO>) {
                //I funksjonen får vi tilbake en response, dette er dataklassen
                val myLocationList = response.body()!! //inneholdet i dataklassen

                updateLocationName(myLocationList.meta.location.name)
                areaName = myLocationList.meta.superlocation.name
                getCurrentTime()

                updateTextView()
                setValues(myLocationList)
            }
        })
    }
}
