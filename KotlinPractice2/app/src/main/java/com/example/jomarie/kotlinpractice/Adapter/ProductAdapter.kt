package com.example.jomarie.kotlinpractice.Adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.jomarie.kotlinpractice.Model.Product
import com.example.jomarie.kotlinpractice.R

/**
 * Created by jomarie on 3/21/2018.
 */
class ProductAdapter(var productlist: ArrayList<Product>, val delegate: Delegate) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    var num : Int = 1
    interface Delegate {
        fun onClickProduct(product: Product)
    }



    override fun onBindViewHolder(p0: ViewHolder?, position: Int) {
        val product: Product =  productlist[position]
        p0?.textViewName?.text = product.productname
        p0?.textViewPrice?.text = "$ " + product.price

        Glide.with(p0?.itemView!!.context).load("http://192.168.1.110:8080/Ecommerce/assets/images/" + product.image).into(p0?.productImage)

        p0?.productCard?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                delegate.onClickProduct(productlist.get(position))
            }
        })

    }

    override fun getItemCount(): Int {
        return if(num*5 > productlist.size){
            productlist.size
        }else{
            num*5
        }
        return productlist.size

    }

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.list_layout, p0, false)
        return ViewHolder(v)
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val textViewName    = itemView.findViewById<View>(R.id.textViewName) as TextView
        val textViewPrice   = itemView.findViewById<View>(R.id.txtViewPrice) as TextView
        val productImage    = itemView.findViewById<View>(R.id.productImage) as ImageView
        val productCard     = itemView.findViewById<View>(R.id.productCard)  as LinearLayout
    }


}