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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.example.jomarie.kotlinpractice.Adapter.ProductAdapter
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.Product
import com.example.jomarie.kotlinpractice.Model.User
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_category_product.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.ArrayList

class MainActivity : AppCompatActivity(), ProductAdapter.Delegate {
    private val apiService by lazy {
        ApiInterface.create()
    }

    lateinit var mHandler     : Handler
    lateinit var mRunnable    : Runnable
    var mSwipeRefreshLayout   : SwipeRefreshLayout?         = null
    private var loginDialog   : DialogInterface?            = null

    private var mUserInfoList: java.util.ArrayList<User>?   = null
    private var sharedPreferences : SharedPreferences?      = null
    private var progressDialog : ProgressDialog?            = null
    private var mAndroidArrayList: ArrayList<Product>?      = null
    private var mAdapter: ProductAdapter?                   = null

    var disposable : Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSwipeRefreshLayout = findViewById<View>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        initRecyclerView()
        mHandler = Handler()

        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)

        progressDialog = indeterminateProgressDialog("Loading Data..")
        progressDialog?.setCancelable(false)
        progressDialog!!.show()
        loadProduct("","rifle", rifleRecycler)
        loadProduct("","grenade", grenadeRecycler)

        val txtRifle    = findViewById<TextView>(R.id.txtRifle)
        txtRifle.setOnClickListener {
            startActivity<CategoryProduct>("category" to "rifle")
        }
        val txtGrenade  = findViewById<TextView>(R.id.txtGrenade)
        txtGrenade.setOnClickListener {
            startActivity<CategoryProduct>("category" to "grenade")
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


    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        grenadeRecycler.setHasFixedSize(true)
        grenadeRecycler.layoutManager = layoutManager

        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rifleRecycler.setHasFixedSize(true)
        rifleRecycler.layoutManager = layoutManager2
    }

    fun loadProduct(query : String, category : String, productrecycler: RecyclerView){
        disposable = apiService.getProductDetails(query, category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> rifleResponse(result, productrecycler)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }


    private fun rifleResponse(productList: List<Product>, productrecycler : RecyclerView) {
        mAndroidArrayList = ArrayList(productList)
        mAdapter = ProductAdapter(mAndroidArrayList!!, this)

        productrecycler.adapter = mAdapter
        progressDialog?.dismiss()
    }


    override fun onClickProduct(product: Product) {
        startActivity<ProductDetails>("id" to product.id, "product" to product.productname, "price" to product.price, "description" to product.description, "image" to product.image )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.account -> {
                val logged = sharedPreferences?.getBoolean("LOGGED", false)

                if(logged!!){
                    val id       = sharedPreferences?.getInt("id", 0)
                    val name     = sharedPreferences?.getString("name", "")
                    val email    = sharedPreferences?.getString("email","")
                    val contact  = sharedPreferences?.getInt("contact", 0)
                    val address  = sharedPreferences?.getString("address","")

                    startActivity<AccountProfile>("id" to id, "name" to name, "email" to email, "contact" to contact, "address" to address)
                }else{
                    loginAlert()
                }
                return true

            }
            R.id.cart ->{
                startActivity<Cart>()
                return true
            }
            else-> return super.onOptionsItemSelected(item)
        }
    }

    fun loginAlert(){
        this.loginDialog = alert {
            title = "Login Account"
            customView {
                verticalLayout {
                    padding = dip(15)
                    val username = editText(){
                        hint        = "username"
                        textSize    = sp(10).toFloat()
                        width       = dip(100)
                    }.lparams{ width = matchParent }

                    val password = editText() {
                        hint        = "password"
                        textSize    = sp(10).toFloat()
                        width       = dip(100)
                        inputType   = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }.lparams{ width = matchParent }

                    textView("Don't Have An Account?"){
                        textColor = Color.BLUE
                        onClick {
                            startActivity<Register>()
                        }
                    }
                    button("Login"){
                        onClick{
                            if(username.text.isEmpty() || password.text.isEmpty()){
                                toast("All fields are required")
                            }else{
                                progressDialog = indeterminateProgressDialog("Logging in..")
                                progressDialog!!.show()
                                login(username.text.toString(), password.text.toString())
                                this@MainActivity.loginDialog?.dismiss()
                            }
                        }
                    }.lparams{width = matchParent }
                }
            }
        }.show()
    }

    fun login(username : String, password: String){
        disposable = apiService.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> loginResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
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
                editor?.putInt("contact", user.contact)
                editor?.putString("address", user.address)
                editor?.apply()
            }
        }else{
            longToast("Incorrect Username or Password")
        }
        progressDialog?.dismiss()
    }
}
