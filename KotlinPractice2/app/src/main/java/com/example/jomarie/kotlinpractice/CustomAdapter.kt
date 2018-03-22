package com.example.jomarie.kotlinpractice

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Created by jomarie on 3/21/2018.
 */
class CustomAdapter(val productlist: ArrayList<Product>, val delegate: CustomAdapter.Delegate) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    interface Delegate {
        fun onClickProduct(product: Product)
    }
    override fun onBindViewHolder(p0: ViewHolder?, position: Int) {
        val product: Product =  productlist[position]
        p0?.textViewName?.text = product.productname
        p0?.textViewCode?.text = product.productcode
        p0?.textViewPrice?.text = "$ " + product.price_output

        p0?.product_container?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                delegate.onClickProduct(productlist.get(position))
            }
        })

    }

    override fun getItemCount(): Int {
        return productlist.size
    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.list_layout, p0, false)
        return ViewHolder(v)
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val textViewName = itemView.findViewById<View>(R.id.textViewName) as TextView
        val textViewCode = itemView.findViewById<View>(R.id.textViewCode) as TextView
        val textViewPrice = itemView.findViewById<View>(R.id.txtViewPrice) as TextView
        val product_container = itemView.findViewById<LinearLayout>(R.id.background)
    }
}