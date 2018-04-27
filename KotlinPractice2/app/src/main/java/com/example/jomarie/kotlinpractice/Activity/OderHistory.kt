package com.example.jomarie.kotlinpractice.Activity


import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.jomarie.kotlinpractice.Adapter.OrderAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.Order
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_oder_history.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class OderHistory : AppCompatActivity() , OrderAdapter.Delegate{
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var progressDialog    : ProgressDialog?     = null
    private var mOrderArrayList   : ArrayList<Order>?   = null
    private var mAdapter          : OrderAdapter?       = null
    private var id                : Int                 = 0
    private var disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oder_history)

        id = intent.getIntExtra("user_id", 0)

        initRecyclerView()
        load_Order()
    }

    private fun initRecyclerView() {
        recyclerOrder.setHasFixedSize(true)

        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerOrder.layoutManager = layoutManager
    }


    private fun load_Order(){
        progressDialog = indeterminateProgressDialog("Loading Orders")
        progressDialog?.show()
        disposable.add(apiService.getTransaction(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> response(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }

    private fun response(orderList: List<Order>) {
        progressDialog?.dismiss()
        mOrderArrayList = ArrayList(orderList)
        mAdapter = OrderAdapter(mOrderArrayList!!, this)
        recyclerOrder.adapter = mAdapter

        if(mOrderArrayList!!.isEmpty()){
            opNullReport.visibility = View.VISIBLE
        }
    }

    override fun onClickTransaction(orderList: Order) {
        startActivity<OrderedProductsActivity>("transac_num" to orderList.transac_num)
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }


}
