package com.example.jomarie.kotlinpractice.Adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Model.Order
import com.example.jomarie.kotlinpractice.R
import android.widget.LinearLayout
import android.view.animation.AnimationUtils
import android.widget.Button


/**
 * Created by jomarie on 4/24/2018.
 */
class OrderAdapter(var orderList: ArrayList<Order>, val delegate : Delegate) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    interface Delegate {
        fun onClickTransaction(orderList: Order)
    }

    private var currentPosition = 0


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val order : Order  = orderList[position]
        holder?.transac_num?.text       = "Transaction #${order.transac_num}"
        holder?.transac_quantity?.text  = "Subtotal (${order.itemQuantity} item/s)"
        holder?.transac_totalA?.text    = "$ ${order.totalAmount}"

        holder?.linearLayout?.visibility = View.GONE
        holder?.transac_num?.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_keyboard_arrow_down_black_24dp,0)

        //if the position is equals to the item position which is to be expanded
        if (currentPosition == position) {
            //creating an animation
            val slideDown = AnimationUtils.loadAnimation(holder?.itemView?.context, R.anim.slide_down)

            //toggling visibility
            holder?.linearLayout?.visibility = View.VISIBLE
            holder?.transac_num?.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)

            //adding sliding effect
            holder?.linearLayout?.startAnimation(slideDown)
        }

        holder?.transac_num?.setOnClickListener {
            //getting the position of the item to expand it
            currentPosition = position

            //reloding the list
            notifyDataSetChanged()
        }

        holder?.btnSeeProducs?.setOnClickListener (object : View.OnClickListener{
            override fun onClick(p0: View?) {
                delegate.onClickTransaction(orderList.get(position))
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_order, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val transac_num       = itemView.findViewById<View>(R.id.transac_num)           as TextView
        val transac_quantity  = itemView.findViewById<View>(R.id.transac_itemquantity)  as TextView
        val transac_totalA    = itemView.findViewById<View>(R.id.transac_total)         as TextView
        val btnSeeProducs     = itemView.findViewById<View>(R.id.btnShowProducts)       as Button

        val linearLayout      = itemView.findViewById<View>(R.id.linearLayout)          as LinearLayout

    }

}