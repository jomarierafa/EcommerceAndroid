package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import org.jetbrains.anko.*
import java.util.*
import kotlin.concurrent.schedule
import android.support.v7.widget.GridLayoutManager
import android.text.InputType
import com.example.jomarie.kotlinpractice.Adapter.ProductAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.Product
import com.example.jomarie.kotlinpractice.Model.User
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_category_product.*


import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.sdk25.coroutines.onClick

class CategoryProduct : AppCompatActivity(), ProductAdapter.Delegate {

    private val apiService by lazy {
        ApiInterface.create()
    }

    lateinit var mHandler           : Handler
    lateinit var mRunnable          : Runnable
    private var mSwipeRefreshLayout : SwipeRefreshLayout?      = null
    private var lLayout             : GridLayoutManager?       = null
    private var progressDialog      : ProgressDialog?          = null
    private var mAndroidArrayList   : ArrayList<Product>?      = null
    private var mAdapter            : ProductAdapter?          = null

    private var category   : String?        = null
    private var disposable : Disposable?    = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_product)

        mSwipeRefreshLayout = findViewById<View>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        initRecyclerView()
        mHandler = Handler()

        category = intent.getStringExtra("category")
        progressDialog = indeterminateProgressDialog("Loading Data..")
        progressDialog?.setCancelable(false)
        progressDialog!!.show()
        loadProduct("", category.toString())


        //swipeRefresh
        mSwipeRefreshLayout!!.setOnRefreshListener {
            mRunnable = Runnable {
                loadProduct("", category.toString())
                mSwipeRefreshLayout!!.isRefreshing = false
            }
            mHandler.postDelayed(
                    mRunnable,
                    (500).toLong()
            )

        }
    }

    private fun initRecyclerView() {
        recycler.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 2)
        lLayout = GridLayoutManager(this, 4)
        recycler.layoutManager = layoutManager
    }


    fun loadProduct(query : String, category : String){
        disposable = apiService.getProductDetails(query, category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> handleResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    private fun handleResponse(productList: List<Product>) {
        mAndroidArrayList = ArrayList(productList)
        mAdapter = ProductAdapter(mAndroidArrayList!!, this)

        recycler.adapter = mAdapter
        progressDialog?.dismiss()
    }

    override fun onClickProduct(product: Product) {
        startActivity<ProductDetails>("id" to product.id, "product" to product.productname, "price" to product.price, "description" to product.description, "image" to product.image )
    }

    //toolbar menus
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
                        this@CategoryProduct.runOnUiThread(java.lang.Runnable {
                            loadProduct(p0!!, category.toString())
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


}
