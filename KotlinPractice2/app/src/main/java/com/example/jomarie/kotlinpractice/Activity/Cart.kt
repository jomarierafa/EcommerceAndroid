package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType.TYPE_CLASS_NUMBER
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Adapter.CartAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cart.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.ArrayList

class Cart : AppCompatActivity(), CartAdapter.Delegate{
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var txtTotalAmount   : TextView?                 = null
    private var progressDialog   : ProgressDialog?           = null
    private var itemQDialog      :  DialogInterface?         = null
    private var detailsDialog    : DialogInterface?          = null

    private var mCartArrayList   : ArrayList<CartProduct>?   = null
    private var mAdapter         : CartAdapter?              = null
    private var sharedPreferences: SharedPreferences?        = null
    private var disposable       : Disposable?               = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        txtTotalAmount = findViewById<View>(R.id.totalAmount) as TextView
        initRecyclerView()

        progressDialog = indeterminateProgressDialog("Loading Cart...")
        progressDialog!!.show()
        loadCart()

        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        val logged  = sharedPreferences?.getBoolean("LOGGED", false)
        val name    = sharedPreferences?.getString("name", "")
        val email   = sharedPreferences?.getString("email", "")
        val contact = sharedPreferences?.getInt("contact", 0)
        val address = sharedPreferences?.getString("address", "")

        val btnCheckOut = findViewById<Button>(R.id.btnCheckOut)
        btnCheckOut.setOnClickListener {
            if(mCartArrayList?.size!! > 0) {
                if(logged!!){
                    showDetails(name!!, email!!, contact!!, address!!)
                }else {
                    startActivity<Transaction>()
                    finish()
                }
            }else{longToast("Cart is Empty")}
        }

        val tf = Typeface.createFromAsset(assets, "fonts/Shenanigans.ttf")
        btnCheckOut.typeface = tf

    }

    private fun initRecyclerView() {
        cartRecycler.setHasFixedSize(true)
        //val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        cartRecycler.layoutManager = layoutManager
    }

    //load cart data
    fun loadCart(){
        disposable = apiService.showCart()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> loadCartResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }
    private fun loadCartResponse(cartList : List<CartProduct>) {
        mCartArrayList = ArrayList(cartList)
        mAdapter = CartAdapter(mCartArrayList!!, this)
        cartRecycler.adapter = mAdapter

        var totalAmount: Double? = 0.0
        for(i in mCartArrayList!!){
            totalAmount = totalAmount!!.toDouble() + i.totalamount
        }
        txtTotalAmount!!.text = "$ " + totalAmount.toString()

        progressDialog?.dismiss()
    }

    //remove product from cart
    fun removeFromCart(product_id: Int){
        disposable = apiService.removeFromCart(product_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> loadCart()
                                  showMessage("Product Remove Successful")},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    //add item or deduct
    fun addItemQuantity(oldQty: Int, inputQty: Int, price: Float, code: String, stock: Int, product_id: Int, operation: String){
        disposable = apiService.addItemQuantity(oldQty, inputQty, price, code, stock, product_id, operation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> showMessage(result.response2)
                                  loadCart()
                                  progressDialog?.dismiss()},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    override fun onClickAdd(product: CartProduct) {
        editItemQuantity(product, "Add")
    }
    override fun onClickMinus(product: CartProduct, holder: CartAdapter.ViewHolder?) {
        editItemQuantity(product, "Deduct")
    }
    override fun onClickRemove(product: CartProduct) {
        progressDialog = indeterminateProgressDialog("Removing Product...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
        removeFromCart(product.id)
    }

    //dialogs
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
                            if(quantity.text.isEmpty()){
                                toast("enter quantity")
                            }else{
                                addItemQuantity(product.qty, quantity.text.toString().toInt(), product.amount, product.code!!,product.stock,product.id, operation)
                                this@Cart.itemQDialog?.dismiss()
                                progressDialog = indeterminateProgressDialog(operation + "...")
                                progressDialog!!.show()
                            }
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

    //saving transaction if user is loggedin
    fun addTransaction(name: String, email: String, contact: Int, address: String){
        disposable = apiService.saveTransaction(name, email, contact, address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> loadCart()
                                  showMessage("Order Successful")
                                  progressDialog?.dismiss()},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    fun showDetails(name: String, email : String, contact : Int, address: String){
        this.detailsDialog = alert {
            title = "Your Transaction Details"
            customView {
                verticalLayout {
                    padding = dip(15)
                    textView("Name: $name"){
                        textSize = sp(10).toFloat()
                    }.lparams{ width = matchParent }
                    textView("Email: $email") {
                        textSize  = sp(10).toFloat()
                    }.lparams{ width = matchParent }
                    textView("Contact: $contact") {
                        textSize  = sp(10).toFloat()
                    }.lparams{ width = matchParent }
                    textView("Address: $address") {
                        textSize  = sp(10).toFloat()
                    }.lparams{ width = matchParent }


                    button("Submit"){
                        onClick{
                            progressDialog = indeterminateProgressDialog("Saving...")
                            progressDialog!!.show()
                            addTransaction(name,email,contact,address)
                            this@Cart.detailsDialog?.dismiss()
                        }
                    }.lparams{width = matchParent }
                    button("Switch Account"){
                        onClick{
                            sharedPreferences!!.edit().clear().apply()
                            startActivity<Transaction>()
                            finish()
                        }
                    }.lparams{width = matchParent }


                }
            }
        }.show()
    }



}
