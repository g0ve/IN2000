package com.example.gronnprogrammering.Settings
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gronnprogrammering.R
import kotlinx.android.synthetic.main.activity_countyelement.view.*
import java.io.*

class CountyListAdapter(val context: Context, val elementer: ArrayList<String>) : RecyclerView.Adapter<CountyListAdapter.minViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): minViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_countyelement, parent, false)
        return minViewHolder(view)
    }

    override fun getItemCount(): Int {
        return elementer.size
    }

    override fun onBindViewHolder(p0: minViewHolder, p1: Int) {
        val element = elementer[p1]
        p0.setData(element, p1)
    }

    //Change posistion on elements when user wants too
    fun swapItems(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition..toPosition - 1) {
                elementer.set(i, elementer.set(i+1, elementer[i]));
            }
        } else {
            for (i in fromPosition..toPosition + 1) {
                elementer.set(i, elementer.set(i-1, elementer[i]));
            }
        }
        setCounties(elementer)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun removeItem(position: Int){
        elementer.removeAt(position)
        setCounties(elementer)
        notifyItemRemoved(position)
    }

    fun addItem(name: String){
        elementer.add(name)
        setCounties(elementer)
        notifyItemInserted(elementer.size)
    }


    inner class minViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun setData(element: String?, posisjon: Int){
            itemView.tittel.text = element
        }
    }

    //Writes to file
    fun setCounties(cities: ArrayList<String>){

        val path = context.filesDir
        val file = File(path, "CountyList.txt")
        val writer = BufferedWriter(FileWriter(file, false))
        for (element in cities) {
            writer.write(element + "\n")
        }
        writer.close()

    }



}