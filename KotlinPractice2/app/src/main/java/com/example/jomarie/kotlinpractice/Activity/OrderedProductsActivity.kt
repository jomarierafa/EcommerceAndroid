package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.example.jomarie.kotlinpractice.Adapter.OProductAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.OrderedProduct
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_oder_history.*
import kotlinx.android.synthetic.main.activity_ordered_products.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.toast

class OrderedProductsActivity : Activity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var mOrderedArrayList   : ArrayList<OrderedProduct>?   = null
    private var mAdapter         : OProductAdapter?             = null
    private var transac_num      : String                       = ""
    private var disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordered_products)

        transac_num = intent.getStringExtra("transac_num")

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        val width  : Int = dm.widthPixels
        val height : Int = dm.heightPixels


        window.setLayout(width,height)

        val params : WindowManager.LayoutParams = window.attributes
        params.gravity = Gravity.CENTER

        params.x = 0
        params.y = -20

        window.attributes = params
        popupwindow.backgroundDrawable = ColorDrawable(android.graphics.Color.TRANSPARENT)


        initRecyclerView()
        loadProduct()

    }

    private fun initRecyclerView() {
        opRecycler.setHasFixedSize(true)

        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        opRecycler.layoutManager = layoutManager
    }
    private fun loadProduct(){
        disposable.add(apiService.getOrderedProduct(transac_num)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> Response(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }

    private fun Response(orderList : List<OrderedProduct>) {
        mOrderedArrayList = ArrayList(orderList)
        mAdapter = OProductAdapter(mOrderedArrayList!!)
        opRecycler.adapter = mAdapter
        opLoading.visibility = View.INVISIBLE

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}
