package com.example.jomarie.kotlinpractice

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by jomarie on 3/21/2018.
 */
class CustomAdapter(val userlist: ArrayList<Product>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    override fun onBindViewHolder(p0: ViewHolder?, p1: Int) {
        val user: Product =  userlist[p1]
        p0?.textViewName?.text = user.productname
        p0?.textViewAddress?.text = user.code
        p0?.textViewPrice?.text = "$ " + user.price_output

    }

    override fun getItemCount(): Int {
        return userlist.size
    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.list_layout, p0, false)
        return ViewHolder(v)
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val textViewName = itemView.findViewById<View>(R.id.textViewName) as TextView
        val textViewAddress = itemView.findViewById<View>(R.id.textViewCode) as TextView
        val textViewPrice = itemView.findViewById<View>(R.id.txtViewPrice) as TextView
    }
}