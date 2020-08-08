package com.example.gronnprogrammering.Settings
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.gronnprogrammering.DTO.CountyDTO
import com.example.gronnprogrammering.R
import com.example.gronnprogrammering.RetrofitClientInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.preference.PreferenceManager
import android.os.PersistableBundle
import com.example.gronnprogrammering.GetCalls


class SettingsFragment : Fragment() {

    val TAG = "Varsling_Settings"
    val SHAREDBOOL = "sharedbool"
    val SHAREDSTRING = "sharedstring"
    val bundle = PersistableBundle()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val aqichosen = arrayListOf(0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)

        var switch_1 = view?.findViewById<Switch>(R.id.switch1)

        var colorblind = view?.findViewById<CheckBox>(R.id.colorblind)

        //We set switchstate from shared preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        switch_1!!.isChecked = prefs.getBoolean(SHAREDBOOL, false)
        if (switch_1.isChecked){
            switch_1.text = prefs.getString(SHAREDSTRING, "Notifikasjon er av")
        }


        val allCountyObjects = ArrayList<CountyDTO>()
        val allCountyStrings = ArrayList<String>()

        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllCounty()

        call?.enqueue(object : Callback<List<CountyDTO>> {
            override fun onFailure(call: Call<List<CountyDTO>>, t: Throwable) {
                Toast.makeText(activity, "Det oppstod en feil, prøv igjen senere", Toast.LENGTH_LONG).show()
            }
            override fun onResponse(call: Call<List<CountyDTO>>, response: Response<List<CountyDTO>>) {
                val countyElements = response.body()
                for (county in countyElements!!) {
                    allCountyObjects.add(county)
                    allCountyStrings.add(county.name)
                }
            }
        })



        //Colorblind mode
        colorblind?.isChecked = prefs.getBoolean("colorblind", false)
        colorblind?.setOnClickListener{
            if(!colorblind.isChecked) {
                prefs.edit().putBoolean("colorblind", false).commit()
            }
            else{
                prefs.edit().putBoolean("colorblind", true).commit()
            }
        }

        //Switch for turning notifications on and off (opens alertdialog)
        switch_1?.setOnClickListener{
            if (switch_1.isChecked) {
                val alertDialogView = LayoutInflater.from(context).inflate(R.layout.settings_alertdialog2, null)
                val msg = TextView(context)
                msg.text = "Opprett notifikasjon hvis AQI i gitt kommune er høyere enn valgt"
                msg.textSize = 20.toFloat()
                msg.setTextColor(Color.parseColor("#000000"))

                val builder = AlertDialog.Builder(context!!)
                    .setView(alertDialogView)
                    .setCustomTitle(msg)
                val tmpalertdialog = builder.show()
                val spinner = alertDialogView.findViewById<Spinner>(R.id.aqilevel)

                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, aqichosen)
                spinner.adapter = adapter

                val kommuneinput = alertDialogView.findViewById<AutoCompleteTextView>(R.id.kommuneinput)
                val suggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, allCountyStrings)
                kommuneinput.setAdapter(suggestionsAdapter)
                kommuneinput.threshold = 1

                val addnotificationbutton = alertDialogView.findViewById<Button>(R.id.addnotification)
                addnotificationbutton.setOnClickListener {
                    tmpalertdialog.cancel()

                    if (kommuneinput.text.toString().isEmpty()) {
                        Toast.makeText(context, "Dette er ikke et gyldig kommunenavn, Ingen notifikasjon opprettet", Toast.LENGTH_LONG).show()
                        switch_1.isChecked = false
                        switch_1.text = "Notifikasjon er av"
                        prefs.edit().putBoolean(SHAREDBOOL, false).commit()
                        tmpalertdialog.cancel()
                    } else if (!allCountyStrings.contains(kommuneinput.text.toString())) {
                        Toast.makeText(context, "Dette er ikke et gyldig kommunenavn, ingen notifikasjon opprettet", Toast.LENGTH_LONG).show()
                        switch_1.isChecked = false
                        switch_1.text = "Notifikasjon er av"
                        prefs.edit().putBoolean(SHAREDBOOL, false).commit()
                        tmpalertdialog.cancel()
                    } else {
                        //Creates notification
                        bundle.putString("Location", kommuneinput.text.toString())
                        bundle.putDouble("AQI", spinner.selectedItem as Double)
                        for (e : CountyDTO in allCountyObjects){
                            if(e.name.equals(kommuneinput.text.toString())){
                                bundle.putString("Latitude", e.latitude)
                                bundle.putString("Longitude", e.longitude)
                            }
                        }

                        scheduleJob(view)

                        Toast.makeText(context, "Opprettet notifikasjon over " + spinner.selectedItem.toString() + " AQI i " + kommuneinput.text.toString(), Toast.LENGTH_LONG).show()
                        switch_1.text = "Notifikasjon ved over " + spinner.selectedItem.toString() + " AQI i " + kommuneinput.text.toString()
                        switch_1.isChecked = true
                        prefs.edit().putBoolean(SHAREDBOOL, true).commit()
                        prefs.edit().putString(SHAREDSTRING, "Notifikasjon ved over " + spinner.selectedItem.toString() + " AQI i " + kommuneinput.text.toString()).commit()
                    }
                }
            }
            else {
                if (switch_1.text != "Notifikasjon er av") {
                    val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                //Turns notification off
                                cancelJob(view)
                                switch_1.text = "Notifikasjon er av"
                                prefs.edit().putBoolean(SHAREDBOOL, false).commit()
                            }

                            DialogInterface.BUTTON_NEGATIVE -> {
                                switch_1.isChecked = true
                                prefs.edit().putBoolean(SHAREDBOOL, true).commit()
                            }
                        }
                    }

                    val builder = AlertDialog.Builder(context!!)
                    builder.setMessage("Er du sikker på at du vil skru av notifikasjon?").setPositiveButton("Ja", dialogClickListener)
                        .setNegativeButton("Nei", dialogClickListener).show()

                    Toast.makeText(context, "Notifikasjon er av", Toast.LENGTH_LONG).show()
                }

            }

            if(switch_1.text == "Notifikasjon er av" && switch_1.isChecked){
                switch_1.isChecked = false
                prefs.edit().putBoolean(SHAREDBOOL, false).commit()
            }

        }


        val button2 = view?.findViewById<Button>(R.id.button1)
        button2?.setOnClickListener {
            val intent = Intent(activity, CountylistActivity:: class.java)
            startActivity(intent)
        }

        return view
    }
    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }


    //Method for planning a notification jobs
    fun scheduleJob(v : View){
        val componentname = ComponentName(context, NotificationService::class.java!!.getName())
        val infobuilder = JobInfo.Builder(1, componentname)
        infobuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        infobuilder.setPersisted(true)
        infobuilder.setPeriodic(6 * 60 * 60 * 1000) ///Sjekker conditions for varsling hver 6 time
        infobuilder.setExtras(bundle)
        val info = infobuilder.build()
        var scheduler = context?.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        var resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job scheduled")
        }
        else{
            Log.d(TAG, "Job Scheduling Failed!")
        }
    }

    fun cancelJob(v : View){
        val scheduler = context?.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(1)
        Log.d(TAG, "Job cancelled")
    }
}
