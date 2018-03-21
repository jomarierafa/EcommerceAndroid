package com.example.jomarie.kotlinpractice

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback

class MainActivity : AppCompatActivity() {
    val users = ArrayList<Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState   )
        setContentView(R.layout.activity_main)

        DisplayProgressDialog()
        callWebService()
    }

    lateinit var pDialog: ProgressDialog
    fun DisplayProgressDialog() {

        pDialog = ProgressDialog(this@MainActivity)
        pDialog!!.setMessage("Loading..")
        pDialog!!.setCancelable(false)
        pDialog!!.isIndeterminate = false
        pDialog!!.show()
    }

    fun callWebService(){
        val apiService = ApiInterface.create()
        val call = apiService.getCategoryDetails()
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: retrofit2.Response<ProductResponse>?) {
                if (response != null) {
                    if (pDialog != null && pDialog!!.isShowing()) {
                        pDialog.dismiss()
                    }
                    var list: List<ProductSerialized> = response.body().categories!!

                    for (item: ProductSerialized in list.iterator()) {
                        users.add(Product(item.productcode.toString(),item.productname.toString(), item.quantity_output, item.price_output.toString()))
                    }

                    val recyclerView = findViewById<View>(R.id.recycler) as RecyclerView
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayout.VERTICAL, false)
                    val adapter = CustomAdapter(users)
                    recyclerView.adapter = adapter

                }
            }
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable ) {
                Toast.makeText(this@MainActivity, "Error  \n  ", Toast.LENGTH_LONG).show()
            }
        })



    }
}
