package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType.TYPE_CLASS_NUMBER
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Adapter.CartAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.R
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Cart : AppCompatActivity(), CartAdapter.Delegate {
    private var recyclerView : RecyclerView? = null
    private var txtTotalAmount : TextView? = null
    var list : ArrayList<CartProduct> = ArrayList<CartProduct>()
    var progressDialog: ProgressDialog? = null
    var itemQDialog: DialogInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        txtTotalAmount = findViewById<View>(R.id.totalAmount) as TextView
        recyclerView = findViewById<View>(R.id.cartRecycler) as? RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        progressDialog = indeterminateProgressDialog("Loading Cart...")
        progressDialog!!.show()
        callWebService()

        val btnCheckOut = findViewById<Button>(R.id.btnCheckOut)
        btnCheckOut.setOnClickListener {
            if(list.size < 0) {
                startActivity<Transaction>()
                finish()
            }else{longToast("Cart is Empty")}
        }
    }

    fun callWebService(){
        val apiService = ApiInterface.create()
        val call = apiService.showCart()
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>?) {
                if (response != null) {
                    list = response!!.body()!!.cartproducts!!
                    val adapter = CartAdapter(list, this@Cart)
                    recyclerView!!.adapter = adapter

                    //adding totalAmount
                    var totalAmount: Int? = 0
                    for(i in list){
                        totalAmount = totalAmount!!.toInt() + i.totalamount
                    }
                    txtTotalAmount!!.text = "$ " + totalAmount.toString()
                }
                progressDialog!!.dismiss()
            }
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable ) {
                toast("Error " + t)
            }
        })
    }

    fun removeFromCart(produuct_id : Int){
        val apiService = ApiInterface.create()
        val call = apiService.removeFromCart(produuct_id)
        call.enqueue(object : Callback<ProductResponse>{
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable?) {
                toast("Error " + t)
            }

            override fun onResponse(call: Call<ProductResponse>?, response: Response<ProductResponse>?) {
                callWebService()
                showMessage("Product Remove")
            }

        })
    }

    fun addItemQuantity(oldQty: Int, inputQty: Int, price: Int, code: String, stock: Int, product_id: Int, operation: String){
        val apiService = ApiInterface.create()
        val call = apiService.addItemQuantity(oldQty,inputQty,price,code,stock,product_id, operation)
        call.enqueue(object: Callback<ProductResponse>{
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable?) {
                toast("Error" + t)
            }

            override fun onResponse(call: Call<ProductResponse>?, response: Response<ProductResponse>?) {
                toast(response?.body()!!.response2!!)
                callWebService()
                this@Cart.itemQDialog?.dismiss()
            }

        })
    }

    override fun onClickAdd(product: CartProduct) {
        editItemQuantity(product, "Add")
    }

    override fun onClickMinus(product: CartProduct, holder: CartAdapter.ViewHolder?) {
        editItemQuantity(product, "Deduct")
    }
    override fun onClickRemove(product: CartProduct) {
        removeFromCart(product.id)
    }

    fun editItemQuantity(product: CartProduct, operation: String){
        this.itemQDialog = alert {
            title = operation + " Quantity"
            customView {
                verticalLayout {
                    padding = dip(15)
                    val quantity = editText(){
                        textSize = sp(15).toFloat()
                        width = dip(100)
                        inputType = TYPE_CLASS_NUMBER
                    }.lparams{ width = matchParent }

                    button(operation){
                        onClick {
                            addItemQuantity(product.qty, quantity.text.toString().toInt(), product.amount, product.code!!,product.stock,product.id, operation)
                            progressDialog = indeterminateProgressDialog(operation + "...")
                            progressDialog!!.show()
                        }
                    }.lparams{width = matchParent}
                }
            }
        }.show()
    }

    fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {}
            }.show()
        }
    }

}
