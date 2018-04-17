package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
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
    private var user_id          : Int?                      = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        txtTotalAmount = findViewById<View>(R.id.totalAmount) as TextView
        initRecyclerView()

        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        user_id     = sharedPreferences?.getInt("id", 0)
        val logged  = sharedPreferences?.getBoolean("LOGGED", false)
        val name    = sharedPreferences?.getString("name", "")
        val email   = sharedPreferences?.getString("email", "")
        val contact = sharedPreferences?.getString("contact", "")
        val address = sharedPreferences?.getString("address", "")

        progressDialog = indeterminateProgressDialog("Loading Cart...")
        progressDialog!!.show()
        loadCart()
        
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

        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        cartRecycler.layoutManager = layoutManager
    }

    //load cart data
    private fun loadCart(){
        disposable = apiService.showCart(user_id!!)
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
    private fun removeFromCart(product_id: Int){
        disposable = apiService.removeFromCart(product_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->   loadCart()
                                    setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    //add item or deduct
    private fun addItemQuantity(oldQty: Int, inputQty: Int, price: Float, code: String, stock: Int, product_id: Int, operation: String){
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
    private fun editItemQuantity(product: CartProduct, operation: String){
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
    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {}
            }.show()
        }
    }

    //saving transaction if user is loggedin
    private fun addTransaction(name: String, email: String, contact: String, address: String, cardno: Int, expiry: String, cvccode: Int ){
        disposable = apiService.saveTransaction(user_id!!, name, email, contact, address, cardno, expiry, cvccode )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> loadCart()
                                  showMessage("Order Successful")
                                  progressDialog?.dismiss()},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    private fun showDetails(name: String, email : String, contact : String, address: String){
        this.detailsDialog = alert {
            title = "Payment Details"
            customView {
                verticalLayout {
                    padding = dip(15)
                    val cardno = editText{
                        hint     = "card no."
                        textSize = sp(10).toFloat()
                        inputType = TYPE_CLASS_NUMBER
                    }.lparams{ width = matchParent }
                    val expiry = editText{
                        hint      = "Expiry"
                        textSize  = sp(10).toFloat()
                    }.lparams{ width = matchParent }
                    val cvccode = editText {
                        hint      = "CVC Code"
                        textSize  = sp(10).toFloat()
                        inputType = TYPE_CLASS_NUMBER
                    }.lparams{ width = matchParent }


                    button("Submit"){
                        onClick{
                            progressDialog = indeterminateProgressDialog("Saving...")
                            progressDialog!!.show()
                            addTransaction(name,email,contact,address,cardno.text.toString().toInt(),expiry.text.toString(),cvccode.text.toString().toInt())
                            this@Cart.detailsDialog?.dismiss()
                        }
                    }.lparams{width = matchParent }
                    button("Switch Account"){
                        onClick{
                            sharedPreferences!!.edit().clear().apply()
                            setResult(Activity.RESULT_OK, intent.putExtra("msg", "switch"))
                            finish()
                        }
                    }.lparams{width = matchParent }
                }
            }
        }.show()
    }

    override fun onDestroy() {
        setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
        super.onDestroy()
    }
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
        super.onBackPressed()
    }

}
