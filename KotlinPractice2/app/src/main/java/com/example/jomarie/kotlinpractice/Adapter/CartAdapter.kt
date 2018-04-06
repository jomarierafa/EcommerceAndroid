package com.example.jomarie.kotlinpractice.Adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.R
import com.squareup.picasso.Picasso


/**
 * Created by jomarie on 3/31/2018.
 */
class CartAdapter(var cartproduct: ArrayList<CartProduct>, val delegate: Delegate): RecyclerView.Adapter<CartAdapter.ViewHolder>(){

    interface Delegate{
        fun onClickAdd(product: CartProduct)
        fun onClickMinus(product: CartProduct, holder: ViewHolder?)
        fun onClickRemove(product: CartProduct)

    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val product: CartProduct =  cartproduct[position]
        holder?.cartProduct?.text = product.productname
        holder?.cartAmount?.text = "$ " + product.totalamount
        holder?.cartQty?.text = product.qty.toString()

        Picasso.with(holder?.itemView!!.context).load("http://192.168.15.84:8080/Ecommerce/assets/images/" + product.image).into(holder?.productImage)

        holder?.btnAdd?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                delegate.onClickAdd(cartproduct.get(position))
            }
        })
        holder?.btnMinus?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                delegate.onClickMinus(cartproduct.get(position), holder)
            }
        })
        holder?.txtRemove?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                delegate.onClickRemove(cartproduct.get(position))
            }
        })


    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.list_cart, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return cartproduct.size
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val cartProduct     = itemView.findViewById<View>(R.id.cartProduct) as TextView
        val cartAmount      = itemView.findViewById<View>(R.id.cartAmount) as TextView
        val cartQty         = itemView.findViewById<View>(R.id.quantity) as TextView
        val btnAdd          = itemView.findViewById<View>(R.id.btnAdd) as Button
        val btnMinus        = itemView.findViewById<View>(R.id.btnMinus) as Button
        val txtRemove       = itemView.findViewById<View>(R.id.removeProduct) as TextView
        val productImage    = itemView.findViewById<View>(R.id.cartproductImage) as ImageView
    }

}