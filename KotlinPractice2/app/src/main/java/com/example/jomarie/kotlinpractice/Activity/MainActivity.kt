package com.example.jomarie.kotlinpractice.Activity

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Adapter.ProductAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.Product
import com.example.jomarie.kotlinpractice.Model.User
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_category_product.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notification_update_count_layout.*
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity(), ProductAdapter.Delegate {
    private val apiService by lazy {
        ApiInterface.create()
    }

    lateinit var mHandler           : Handler
    lateinit var mRunnable          : Runnable
    private var mSwipeRefreshLayout : SwipeRefreshLayout?           = null

    private var mUserInfoList       : java.util.ArrayList<User>?    = null
    private var sharedPreferences   : SharedPreferences?            = null
    private var settings            : SharedPreferences?            = null
    private var progressDialog      : ProgressDialog?               = null
    private var mAndroidArrayList   : ArrayList<Product>?           = null
    private var mAdapter            : ProductAdapter?               = null
    private var disposable = CompositeDisposable()

    private var notification : RelativeLayout? = null
    private var itemcount    : TextView?       = null
    private var counter      : Int?            = 0
    private var cartCode     : String?         = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val msg = data?.getStringExtra("msg")
        if(requestCode == 20 && data != null){
            if(msg == "switch"){
                loginAlert()
            }else if(msg == "loadcounter"){
                loadCartCounter()
            }
        }
    }

    private fun generateRandom() : String {
        val chars = "0123456789abcdeghijklmnopqrstuvwxyz"
        var word = ""
        for (i in 0..10){
            word +=chars[Math.floor(Math.random() * chars.length).toInt()]
        }
        return word
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
        notification        = findViewById<RelativeLayout?>(R.id.badge_layout1)
        mSwipeRefreshLayout = findViewById<View>(R.id.swipeRefreshLayout) as SwipeRefreshLayout

        mHandler            = Handler()

        sharedPreferences   = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        settings            = getSharedPreferences("checked", 0)

        loadProduct("","rifle", rifleRecycler)
        loadProduct("","grenade", grenadeRecycler)
        loadCartCounter()

        txtRifle.setOnClickListener {
            startActivityForResult<CategoryProduct>(20, "category" to "rifle")
        }
        txtGrenade.setOnClickListener {
            startActivityForResult<CategoryProduct>(20, "category" to "grenade")
        }

        //swipeRefresh
        mSwipeRefreshLayout!!.setOnRefreshListener {
            mRunnable = Runnable {
                loadProduct("","rifle", rifleRecycler)
                loadProduct("","grenade", grenadeRecycler)
                mSwipeRefreshLayout!!.isRefreshing = false
            }
            mHandler.postDelayed(
                    mRunnable,
                    (500).toLong()
            )

        }

        //check if it is first time used
        if(settings!!.getBoolean("first_time", true)){
            settings!!.edit().putString("cartcode", generateRandom()).apply()
            settings!!.edit().putBoolean("first_time", false).apply()
        }
    }

    //declaring recyclerviews
    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        grenadeRecycler.setHasFixedSize(true)
        grenadeRecycler.layoutManager = layoutManager

        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rifleRecycler.setHasFixedSize(true)
        rifleRecycler.layoutManager = layoutManager2
    }

    // count items in cart
    private fun loadCartCounter(){
        cartCode    = settings?.getString("cartcode", "")
        disposable.add(apiService.showCart(cartCode!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->   counter = result.size
                                    invalidateOptionsMenu()},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }

    //load the available product
    private fun loadProduct(query : String, category : String, productrecycler: RecyclerView){
        disposable.add(apiService.getProductDetails(query, category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> response(result, productrecycler)},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }
    private fun response(productList: List<Product>, productrecycler : RecyclerView) {
        mAndroidArrayList = ArrayList(productList)
        mAdapter = ProductAdapter(mAndroidArrayList!!, this)

        productrecycler.adapter = mAdapter
        rifleLoading.visibility = View.INVISIBLE
        grenadeLoadind.visibility = View.INVISIBLE
    }

    //product view
    override fun onClickProduct(product: Product) {
        startActivityForResult<ProductDetails>(20,"id" to product.id, "product" to product.productname, "price" to product.price, "description" to product.description, "image" to product.image )
    }

    //itemMenu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val cart : MenuItem = menu.findItem(R.id.cart)
        MenuItemCompat.setActionView(cart, R.layout.notification_update_count_layout)
        notification =  MenuItemCompat.getActionView(cart) as RelativeLayout

        val cartView : View  = menu.findItem(R.id.cart).actionView
        itemcount = cartView.findViewById(R.id.badge_notification_1)

        itemcount?.text = counter.toString()
        notification!!.setOnClickListener {
            val int = Intent(this@MainActivity, Cart::class.java)
            this.startActivityForResult(int, 20)
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.account -> {
                val logged = sharedPreferences?.getBoolean("LOGGED", false)
                if(logged!!){
                   showAccountProfile()
                }else{
                    loginAlert()
                }
                return true
            }
            else-> return super.onOptionsItemSelected(item)
        }
    }

    private fun showAccountProfile(){
        val id       = sharedPreferences?.getInt("id", 0)
        val name     = sharedPreferences?.getString("name", "")
        val email    = sharedPreferences?.getString("email","")
        val contact  = sharedPreferences?.getString("contact", "")
        val address  = sharedPreferences?.getString("address","")
        val image    = sharedPreferences?.getString("image", "")
        startActivity<AccountProfile>("id" to id, "name" to name, "email" to email, "contact" to contact, "address" to address, "image" to image)
    }

    //login
    private fun loginAlert(){
        val builder  : AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater : LayoutInflater = layoutInflater
        val view : View = inflater.inflate(R.layout.login_dialog, null)
        builder.setView(view)
        val dialog : Dialog = builder.create()

        val username = view.findViewById<EditText>(R.id.usernamevalue)
        val password = view.findViewById<EditText>(R.id.passwordvalue)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            if(username.text.isEmpty() || password.text.isEmpty()){
                toast("All fields are required")
            }else{
                progressDialog = indeterminateProgressDialog("Logging in..")
                progressDialog!!.show()
                login(username.text.toString(), password.text.toString())
                dialog.dismiss()
            }
        }

        val signUp = view.findViewById<TextView>(R.id.signup)
        signUp.setOnClickListener {
            startActivity<Register>()
        }

        dialog.show()
    }
    private fun login(username : String, password: String){
        disposable.add(apiService.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> loginResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }
    private fun loginResponse(response : Response) {
        if(response.response){
            mUserInfoList = response.userprofile
            for(user in mUserInfoList!!){
                val editor = sharedPreferences?.edit()
                editor?.putBoolean("LOGGED", true)
                editor?.putInt("id", user.id)
                editor?.putString("name", user.name)
                editor?.putString("email", user.email)
                editor?.putString("contact", user.contact)
                editor?.putString("address", user.address)
                editor?.putString("image", user.image)
                editor?.apply()
                showAccountProfile()
            }
        }else{
            longToast("Incorrect Username or Password")
        }
        progressDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }



}
