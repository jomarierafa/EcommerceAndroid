package com.example.jomarie.kotlinpractice

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback

class Display : AppCompatActivity(), CustomAdapter.Delegate {

    private var recyclerView : RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        recyclerView = findViewById<View>(R.id.recycler) as? RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        callWebService()
    }

    fun callWebService(){
        val apiService = ApiInterface.create()
        val call = apiService.getProductDetails()
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: retrofit2.Response<ProductResponse>?) {
                if (response != null) {
                    var list: ArrayList<Product> = response!!.body()!!.products!!

                    val adapter = CustomAdapter(list, this@Display)
                    recyclerView!!.adapter = adapter

                }
            }

            override fun onFailure(call: Call<ProductResponse>?, t: Throwable ) {
                Toast.makeText(this@Display, "Error " + t, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onClickProduct(product: Product) {
        Toast.makeText(this@Display, product.productname + product.id + " " + product.quantity_output, Toast.LENGTH_LONG).show()
    }
}
