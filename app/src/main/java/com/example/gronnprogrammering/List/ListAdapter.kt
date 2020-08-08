package com.example.gronnprogrammering.List

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.gronnprogrammering.*
import com.example.gronnprogrammering.DTO.CountyDTO
import com.example.gronnprogrammering.DTO.MyLocationDTO
import com.example.gronnprogrammering.GetCalls
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ListAdapter(val elements: List<CountyDTO>, context: Context) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    val context = context
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Fetch value from each county
        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllMyLocation(elements[position].latitude, elements[position].longitude)
        Log.d("CodeAndroidLocation", " MIN LOKASJON BLE KALT ")

        val enqueue = call?.enqueue(object : Callback<MyLocationDTO> {

            //if not successful
            override fun onFailure(call: Call<MyLocationDTO>, t: Throwable) {

            }

            //if successful
            override fun onResponse(call: Call<MyLocationDTO>, response: Response<MyLocationDTO>) {
                //Data from API
                val locationList = response.body()!!//the list
                elements[position].values = locationList

                //sett name
                holder.name.text = elements[position].name

                //Get the right time
                val myTime = getCurrentTime()
                for(time in locationList.data.time) {
                    if(time.to == myTime){
                        //henter aqi verdi og gjør den til to desimaler
                        val aqi = time.variables.AQI.value
                        val twoDesimalAqi = Math.round(aqi *100.0)/100.0

                        //Sett AQI level
                        holder.textAQI.text = twoDesimalAqi.toString() + " AQI"

                        //No colorblind mode
                        if(!prefs.getBoolean("colorblind", false)){
                            if(aqi>= 4){
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_veryhigh)
                            }
                            if(aqi>=3 && aqi < 4){
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_high)
                            }
                            else if(aqi>=2 && aqi < 3){
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_moderate)
                            }
                            else{
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_low)
                            }
                        }
                        //colorblind mode
                        else {
                            if (aqi >= 4) {
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_colorblindveryhigh)
                            }
                            if (aqi >= 3 && aqi < 4) {
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_colorblindhigh)
                            } else if (aqi >= 2 && aqi < 3) {
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_colorblindmoderate)
                            } else {
                                holder.textAQI.setBackgroundResource(R.drawable.textview_circel_colorblindlow)
                            }
                        }

                    }
                }

            }
        })
        holder.city = elements[position]
    }

    override fun getItemCount() = elements.size
    class ViewHolder(itemView: View, var city: CountyDTO? = null) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, CountyLocationActivity::class.java)

                intent.putExtra("lat", city?.latitude)
                intent.putExtra("long", city?.longitude)
                intent.putExtra("name", city?.name)

                itemView.context.startActivity(intent)
            }
        }
        val name: TextView = itemView.findViewById(R.id.tvName)
        val textAQI: TextView = itemView.findViewById(R.id.tvAqiLevel)
    }

}

fun getCurrentTime(): String{
    val calendar: Calendar = Calendar.getInstance()
    val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH':00:00Z'")
    Log.d("CodeAndroidLocation", "Tiden nå er: " + timeFormat.format(calendar.time))
    return timeFormat.format(calendar.time)
}
