package com.example.gronnprogrammering.Settings
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.example.gronnprogrammering.DTO.MyLocationDTO
import com.example.gronnprogrammering.GetCalls
import com.example.gronnprogrammering.List.getCurrentTime
import com.example.gronnprogrammering.R
import com.example.gronnprogrammering.RetrofitClientInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class NotificationService : JobService() {

    var Location = "none"
    var latitude = "none"
    var longitude = "none"
    var lowestAQI = 0.0
    var currentAQI = -1.0
    var TAG = "Varsling_Notification"
    var jobCancelled = false
    var min = ""



    //Starts the job and sets location and lowest aqi
    override fun onStartJob(params: JobParameters?): Boolean {
            Location = params!!.extras.getString("Location")
            lowestAQI = params!!.extras.getDouble("AQI")
            latitude = params!!.extras.getString("Latitude")
            longitude = params!!.extras.getString("Longitude")
            Log.d(TAG, "Job started")
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters?){
        if(Location != "none") {
            Log.d(TAG, "Background Job Started")
            Thread(Runnable {
                callApi()
                if (jobCancelled){
                    return@Runnable
                }
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                if(LocalDateTime.now().minute < 10){
                    min = "0"+LocalDateTime.now().minute.toString()
                }
                else{
                    min = LocalDateTime.now().minute.toString()
                }

                if(currentAQI > lowestAQI){
                    //Sends a notification
                    val intent = Intent()
                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notification = Notification.Builder(applicationContext, "CHANNEL_1_ID")
                        .setTicker("")
                        .setContentTitle(LocalDateTime.now().hour.toString() + ":" + min + " - AQI varsling")
                        .setContentText("Målte " + currentAQI.toString() + " AQI i " + Location)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)

                    val prefs = PreferenceManager.getDefaultSharedPreferences(this)

                    //Colorblind mode won't change text color
                    if(prefs.getBoolean("colorblind", false) == true){
                        notification.setColor(Color.WHITE)
                    }
                    else {
                        if (currentAQI >= 4) {
                            notification.setColor(ContextCompat.getColor(applicationContext, R.color.colorAQIVeryHigh))
                        }
                        if (currentAQI >= 3 && currentAQI < 4) {
                            notification.setColor(ContextCompat.getColor(applicationContext, R.color.colorAQIHigh))
                        } else if (currentAQI >= 2 && currentAQI < 3) {
                            notification.setColor(ContextCompat.getColor(applicationContext, R.color.colorAQIModerat))
                        } else {
                            notification.setColor(ContextCompat.getColor(applicationContext, R.color.colorAQILow))
                        }
                    }

                    val finishednotification = notification.build()

                    notificationManager.notify(0, finishednotification)
                    Log.d(TAG, "AQI er målt til " + currentAQI.toString() + " i " + Location + ".")
                }
                Log.d(TAG, "Job Finished")
                jobFinished(params, false)

            }).start()
        }
    }

    //If internet is gone
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    //Calls retorfit on API with the county name for location has parameter, returns AQI-level for county
    private fun callApi(){

        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllMyLocation(latitude, longitude)
        call?.enqueue(object : Callback<MyLocationDTO> {

            //If something went wrong during connection of API
            override fun onFailure(call: Call<MyLocationDTO>, t: Throwable) {
                //Toast.makeText(activity, "Det oppstod en feil, prøv igjen senere", Toast.LENGTH_LONG).show()
            }

            //If connection to API i successful
            override fun onResponse(call: Call<MyLocationDTO>, response: Response<MyLocationDTO>) {
                //In the function we get a response, which contains the county list
                val locationList = response.body()!!//listen

                //Finds current time
                val myTime = getCurrentTime()
                for(time in locationList.data.time) {
                    if(time.to == myTime){
                        val aqi = time.variables.AQI.value
                        currentAQI = Math.round(aqi *100.0)/100.0
                    }

                }

            }
        })
    }
}
