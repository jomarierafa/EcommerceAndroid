package com.example.jomarie.kotlinpractice.Adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.jomarie.kotlinpractice.Model.OrderedProduct
import com.example.jomarie.kotlinpractice.R

/**
 * Created by jomarie on 4/25/2018.
 */
class OProductAdapter(var orderproduct: ArrayList<OrderedProduct>): RecyclerView.Adapter<OProductAdapter.ViewHolder>(){


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val product: OrderedProduct =  orderproduct[position]
        holder?.product?.text = "${product.qty} pcs ${product.product}"
        holder?.amount?.text = "$ " + product.amount

        Glide.with(holder?.itemView!!.context).load("http://192.168.1.110:8080/Ecommerce/assets/images/" + product.image).into(holder?.productImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.list_ordered_product, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orderproduct.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val product         = itemView.findViewById<View>(R.id.oProduct)    as TextView
        val amount          = itemView.findViewById<View>(R.id.oAmount)     as TextView
        val productImage    = itemView.findViewById<View>(R.id.oimage)      as ImageView
    }

}