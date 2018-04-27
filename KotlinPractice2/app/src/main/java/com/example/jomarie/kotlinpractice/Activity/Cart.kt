package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType.TYPE_CLASS_NUMBER
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Adapter.CartAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    private var disposable  = CompositeDisposable()

    private var txtTotalAmount   : TextView?                 = null
    private var progressDialog   : ProgressDialog?           = null
    private var itemQDialog      :  DialogInterface?         = null

    private var mCartArrayList   : ArrayList<CartProduct>?   = null
    private var mAdapter         : CartAdapter?              = null
    private var sharedPreferences: SharedPreferences?        = null
    private var checked          : SharedPreferences?        = null
    private var user_id          : Int?                      = 0
    private var cartCode         : String?                   = null
    private var totalAmount      : Double?                   = 0.0
    private var newString        : String?                   = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        txtTotalAmount = findViewById<View>(R.id.totalAmount) as TextView
        initRecyclerView()

        checked = this.getSharedPreferences("checked", 0)
        cartCode = checked?.getString("cartcode", "")
        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        user_id     = sharedPreferences?.getInt("id", 0)
        val logged  = sharedPreferences?.getBoolean("LOGGED", false)
        val name    = sharedPreferences?.getString("name", "")
        val email   = sharedPreferences?.getString("email", "")
        val contact = sharedPreferences?.getString("contact", "")
        val address = sharedPreferences?.getString("address", "")

        loadCart()
        
        btnCheckOut.setOnClickListener {
            if(mCartArrayList?.size!! > 0) {
                if(logged!!){
                    startActivityForResult<PaymentActivity>(30,"user_id" to user_id,
                            "name" to name.toString(),
                            "email" to email.toString(),
                            "contact" to contact.toString(),
                            "address" to address.toString(),
                            "items" to mCartArrayList!!.size,
                            "totalamount" to newString.toString())
                }else {
                    startActivity<Transaction>("itemQuantity" to mCartArrayList?.size, "totalAmount" to totalAmount.toString())
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
        disposable.add(apiService.showCart(cartCode!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {result-> loadCartResponse(result)},
                                {error-> toast("Error ${error.localizedMessage}")}
                        )
        )
    }
    private fun loadCartResponse(cartList : List<CartProduct>) {
        totalAmount = 0.0
        mCartArrayList = ArrayList(cartList)
        mAdapter = CartAdapter(mCartArrayList!!, this)
        cartRecycler.adapter = mAdapter

        for(i in mCartArrayList!!){
            totalAmount = totalAmount!!.toDouble() + i.totalamount
        }

        newString = String.format("%.2f", totalAmount)
        txtTotalAmount!!.text = "$ " + newString

        cartLoading.visibility = View.INVISIBLE

        if(mCartArrayList!!.isEmpty()){
            cartNullReport.visibility = View.VISIBLE
        }
    }

    //remove product from cart
    private fun removeFromCart(product_id: Int){
        disposable.add(apiService.removeFromCart(product_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->progressDialog?.dismiss()
                                loadCart()
                                setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }

    //add item or deduct
    private fun addItemQuantity(oldQty: Int, inputQty: Int, price: Float, code: String, stock: Int, product_id: Int, operation: String){
        disposable.add(apiService.addItemQuantity(oldQty, inputQty, price, code, stock, product_id, operation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> addItemQuantityResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }
    private fun addItemQuantityResponse(response : Response){
        if(response.response2 == "success"){
            loadCart()
        }else{
            showMessage(response.response2)
        }
        progressDialog?.dismiss()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 30 && data != null) {
            setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
            finish()
        }
    }

    override fun onDestroy() {
        setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
        super.onDestroy()
        disposable.clear()
    }
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
        super.onBackPressed()
    }

}
