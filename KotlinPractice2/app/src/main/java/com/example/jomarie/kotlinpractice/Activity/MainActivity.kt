package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule
import android.support.v7.widget.GridLayoutManager
import com.example.jomarie.kotlinpractice.Adapter.ProductAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.Product
import com.example.jomarie.kotlinpractice.R


class MainActivity : AppCompatActivity(), ProductAdapter.Delegate {
    private var recyclerView: RecyclerView? = null
    var list: ArrayList<Product> = ArrayList<Product>()
    var progressDialog: ProgressDialog? = null
    var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    lateinit var mHandler : Handler
    lateinit var mRunnable: Runnable
    private var lLayout: GridLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSwipeRefreshLayout = findViewById<View>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        recyclerView = findViewById<View>(R.id.recycler) as? RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        lLayout = GridLayoutManager(this, 4)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 2)
        recyclerView!!.layoutManager = layoutManager

        progressDialog = indeterminateProgressDialog("Loading Data..")
        progressDialog!!.show()
        callWebService("")

        mHandler = Handler()

        //swipeRefresh
        mSwipeRefreshLayout!!.setOnRefreshListener {
            mRunnable = Runnable {
                callWebService("")
                mSwipeRefreshLayout!!.isRefreshing = false
            }
            mHandler.postDelayed(
                    mRunnable,
                    (500).toLong()
            )

        }
    }

    fun callWebService(query: String){
        val apiService = ApiInterface.create()
        val call = apiService.getProductDetails(query)
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>?) {
                if (response != null) {
                    list = response!!.body()!!.products!!
                    val adapter = ProductAdapter(list, this@MainActivity)
                    recyclerView!!.adapter = adapter
                }
                progressDialog!!.dismiss()
            }
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable ) {
                longToast("Error " + t)
            }
        })
    }

    fun addToCart(product_Id : Int){
        val apiService = ApiInterface.create()
        val call = apiService.addToCart(product_Id)
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>?, response: Response<ProductResponse>?) {
                if (response!!.body().response!!) {
                    showMessage("Product Added Successfuly")
                } else {
                    showMessage("Product Already in the Cart")
                }
                progressDialog!!.dismiss()
            }
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable?) {
                longToast("Added Error " + t)
            }

        })
    }

    override fun onClickProduct(product: Product) {
        progressDialog = indeterminateProgressDialog("Adding Product..")
        progressDialog!!.show()
        addToCart(product.id)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.menu_search, menu)
        val item: MenuItem = menu.findItem(R.id.menuSearch)
        val searchView: SearchView = item.actionView as SearchView
        searchView.queryHint = "Search Product"

            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }
                override fun onQueryTextChange(p0: String?): Boolean {
                    Timer().schedule(1500){
                        this@MainActivity.runOnUiThread(java.lang.Runnable {
                            callWebService(p0!!)
                        })
                    }

                    return true
                }
            })

        val cart: MenuItem = menu.findItem(R.id.cart)
        cart.setOnMenuItemClickListener {
            startActivity<Cart>()
            return@setOnMenuItemClickListener false
        }
        return super.onCreateOptionsMenu(menu)
    }


    fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {}
            }.show()
        }
    }







}
