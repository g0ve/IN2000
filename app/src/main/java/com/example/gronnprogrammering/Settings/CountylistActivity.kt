package com.example.gronnprogrammering.Settings

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.gronnprogrammering.DTO.CountyDTO
import com.example.gronnprogrammering.R
import com.example.gronnprogrammering.RetrofitClientInstance
import com.example.gronnprogrammering.GetCalls
import kotlinx.android.synthetic.main.activity_settings_countylist.*
import kotlinx.android.synthetic.main.settings_alertdialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CountylistActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        val cities = getCounties()
        val adapter = CountyListAdapter(this, cities)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_countylist)


        val fab = findViewById<View>(R.id.fab)
        val fab2 = findViewById<View>(R.id.fab2)
        val allCountyObjects = ArrayList<CountyDTO>()
        val allCountyStrings = ArrayList<String>()


        val service = RetrofitClientInstance.retrofitInstanse?.create(GetCalls::class.java)
        val call = service?.getAllCounty()

        call?.enqueue(object : Callback<List<CountyDTO>> {
            override fun onFailure(call: Call<List<CountyDTO>>, t: Throwable) {
                Toast.makeText(applicationContext, "Error reading JSON!", Toast.LENGTH_LONG).show()
            }

            //If its connected to the API successfully
            override fun onResponse(call: Call<List<CountyDTO>>, response: Response<List<CountyDTO>>) {
                val countyElements = response.body()//listen
                
                for (county in countyElements!!) {
                    allCountyObjects.add(county)
                    allCountyStrings.add(county.name)
                }
            }
        })
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        rvliste.layoutManager = layoutManager
        rvliste.adapter = adapter

        val callback = DragManageAdapter(adapter, this,
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT))
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(rvliste)

        //Adding value to the list
        fab.setOnClickListener {
            val alertDialogView = LayoutInflater.from(this).inflate(R.layout.settings_alertdialog, null)
            val builder = AlertDialog.Builder(this)
                .setView(alertDialogView)
                .setTitle("Legg til element i lista")

            val tmpalertdialog = builder.show()

            val tittelinput = alertDialogView.findViewById<AutoCompleteTextView>(R.id.tittelinput)
            val suggestionsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, allCountyStrings)
            tittelinput.setAdapter(suggestionsAdapter)
            tittelinput.threshold = 1


            tmpalertdialog.addButton.setOnClickListener {
                try {
                    val input = tittelinput!!.text.toString()

                    if (input.isEmpty()) {
                        Toast.makeText(applicationContext, "Skriv inn et kommunenavn", Toast.LENGTH_LONG).show()
                    }
                    else if (!allCountyStrings.contains(input)) {
                        Toast.makeText(applicationContext, "Dette er ikke et gyldig kommunenavn", Toast.LENGTH_LONG).show()
                    }
                    else {
                        for (kommune in allCountyObjects) {
                            if (kommune.name == input) {
                                if (cities.contains(kommune.name)) {
                                    Toast.makeText(applicationContext, "Denne kommunen er allerede i listen", Toast.LENGTH_LONG).show()
                                } else if (allCountyStrings.contains(kommune.name)) {
                                    adapter.addItem(kommune.name)
                                    Toast.makeText(applicationContext, "La til " + kommune.name + " i listen", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                } catch (ex: Exception) {
                    Toast.makeText(applicationContext, "Noe gikk galt", Toast.LENGTH_LONG).show()
                }
                tmpalertdialog.cancel()
            }

        }

        fab2.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.info_settings_countylist, null)
            val infoDialog = AlertDialog.Builder(dialogView.context)
                .setView(dialogView)
            infoDialog.show()
        }

    }

    //Getting the file as a string
    private fun getCounties(): ArrayList<String>{
        val path = this.filesDir
        val file = File(path, "CountyList.txt")
        if(file.exists() == false) {
            file.createNewFile()
        }
        val contents = file.readText()
        val lines = contents.lines()
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

class DragManageAdapter(adapter: CountyListAdapter, context: Context, dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    private val thisContext = context

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        listAdapter.swapItems(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if(listAdapter.elementer.size <= 1){
                Toast.makeText(thisContext, "Du kan ikke ha en tom liste!", Toast.LENGTH_LONG).show()
                val nummer = viewHolder.adapterPosition
                val elem = listAdapter.elementer[nummer]
                listAdapter.removeItem(viewHolder.adapterPosition)
                listAdapter.addItem(elem)
        }
        else {
            listAdapter.removeItem(viewHolder.adapterPosition)
        }
    }
    private var listAdapter = adapter


}
