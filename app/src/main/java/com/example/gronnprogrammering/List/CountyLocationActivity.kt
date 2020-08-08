package com.example.gronnprogrammering.List

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
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


class CountyLocationActivity : AppCompatActivity() {

    var globalLongitude: String? = ""
    var globalLatitude: String? = ""
    var locationName: String = ""
    var name = ""
    var colorblind: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countylocation)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        colorblind = prefs.getBoolean("colorblind", false)

        globalLatitude = intent.getStringExtra("lat")
        globalLongitude = intent.getStringExtra("long")
        name = intent.getStringExtra("name")

        //Setts on Click listener for the info box
        val infoclick = findViewById(R.id.info_click) as TextView
        infoclick.setOnClickListener {
            if(colorblind) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.info_substances_colorblindpoppop, null)
                val info_dialog = AlertDialog.Builder(dialogView.context)
                    .setView(dialogView)
                info_dialog.show()
            }
            else{
                val dialogView = LayoutInflater.from(this).inflate(R.layout.info_substances_poppop, null)
                val info_dialog = AlertDialog.Builder(dialogView.context)
                    .setView(dialogView)
                info_dialog.show()
            }
        }

        callLocation()
    }


    private fun setValues(myLocationList:MyLocationDTO){
        val tvAQI = findViewById<TextView>(R.id.tvAQI)
        val tvPm10 = findViewById<TextView>(R.id.tvPm10)
        val tvPm25 = findViewById<TextView>(R.id.tvPm25)
        val tvNo2 = findViewById<TextView>(R.id.tvNo2)
        val tvO3 = findViewById<TextView>(R.id.tvO3)

        //text info and click items
        val imgViewArray = arrayListOf<ImageView?>()

        val imgclick1 = findViewById<ImageView>(R.id.imgAir)
        val imgclick2 = findViewById<ImageView>(R.id.imgRunning)
        val imgclick3 = findViewById<ImageView>(R.id.imgMask)
        imgViewArray.add(imgclick1)
        imgViewArray.add(imgclick2)
        imgViewArray.add(imgclick3)
        val textBox = findViewById<TextView>(R.id.info_shortTextView)

        var windowBorder: Int
        var excerciseBorder: Int
        var textBorder: Int

        var textWindow: String
        var textExercise: String
        var text: String


        for(time in myLocationList.data.time) {
            val stringTime:String = getCurrentTime()
            if(time.to == stringTime){
                val aqiLevel = time.variables.AQI.value
                val twoDesimalAqi = Math.round(aqiLevel *100.0)/100.0
                val pollutionPm10 = time.variables.pm10_concentration.value.roundToInt()
                val pollutionPm25 = time.variables.pm25_concentration.value.roundToInt()
                val pollutionNo2 = time.variables.no2_concentration.value.roundToInt()
                val pollutionO3 = time.variables.o3_concentration.value.roundToInt()

                ///Colorblind mode
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

                //not collorblind mode
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

    fun getCurrentTime(): String{
        val calendar: Calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH':00:00Z'")

        return timeFormat.format(calendar.time)
    }

    private fun updateLocationName(name:String){
        locationName = name
    }

    private fun updateTextView(){
        val tvw = findViewById<TextView>(R.id.tvOmrad)
        val tvw2 = findViewById<TextView>(R.id.tvBy)

        tvw?.text = locationName
        tvw2?.text = name
    }


    private fun callLocation(){
        //Uses retrofit to get data from location
        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllMyLocation(this!!.globalLatitude!!, this!!.globalLongitude!!)

        call?.enqueue(object : Callback<MyLocationDTO> {

            //Not successful
            override fun onFailure(call: Call<MyLocationDTO>, t: Throwable) {

            }

            //successful
            override fun onResponse(call: Call<MyLocationDTO>, response: Response<MyLocationDTO>) {
                //Fetch data from API
                val myLocationList = response.body()!!//listen

                updateLocationName(myLocationList.meta.superlocation.name)
                getCurrentTime()

                updateTextView()
                setValues(myLocationList)
            }
        })
    }
}
