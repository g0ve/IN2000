package com.example.gronnprogrammering.List

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.gronnprogrammering.DTO.CountyDTO
import com.example.gronnprogrammering.R
import com.example.gronnprogrammering.RetrofitClientInstance
import com.example.gronnprogrammering.GetCalls
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class CountyFragment :  Fragment() {
    var counties_string = ArrayList<String>()
    var counties_choosen = arrayListOf<CountyDTO>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_countylist, container, false)
        counties_string = getCounties()

        //Fetching data from Retrofit
        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllCounty()

        val recyclerView = view?.findViewById<RecyclerView>(R.id.rv1)
        recyclerView!!.layoutManager = LinearLayoutManager(activity)

        //call on API
        call?.enqueue(object : Callback<List<CountyDTO>> {

            //If not successful
            override fun onFailure(call: Call<List<CountyDTO>>, t: Throwable) {
                Toast.makeText(activity, "Det oppstod en feil, prøv igjen senere", Toast.LENGTH_LONG).show()
            }

            //Is successful
            override fun onResponse(call: Call<List<CountyDTO>>, response: Response<List<CountyDTO>>) {
                //Fetching a list with counties
                val countyElements = response.body()//listen

                //adapter -- creating a layout for each element in the list
                for (county_name in counties_string) {
                    for (county in countyElements!!) {
                        if(county.name == county_name) {
                            counties_choosen.add(county)
                        }
                    }
                }
                val adapter = ListAdapter(counties_choosen, context!!)
                recyclerView.adapter = adapter
            }
        })

        return view
    }
    companion object {
        fun newInstance(): CountyFragment =
            CountyFragment()
    }


    //Fetching a list with the starting elements
    fun getCounties(): ArrayList<String>{
        val path = activity!!.filesDir
        val file = File(path, "CountyList.txt")
        if(file.exists() == false) {
            file.createNewFile()
        }
        val contents = file.readText()
        var lines = contents.lines()
        var arlist = ArrayList<String>()
        for (element in lines){
            if(element != "") {
                arlist.add(element)
            }
        }

        if(contents == ""){
            arlist = arrayListOf("Oslo", "Bergen", "Trondheim", "Stavanger", "Tromsø")
        }
        return  arlist
    }
}
