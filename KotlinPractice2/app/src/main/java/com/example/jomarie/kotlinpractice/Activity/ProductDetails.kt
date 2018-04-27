package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_product_details.*
import org.jetbrains.anko.*


class ProductDetails : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    var disposable = CompositeDisposable()
    var progressDialog  : ProgressDialog? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        detailsProduct.text     = intent.getStringExtra("product")
        detailsPrice.text       = "$ " + intent.getFloatExtra("price", 0F).toString()
        detailsDescription.text = intent.getStringExtra("description")

        val id       = intent.getIntExtra("id", 0)
        val txtimage = intent.getStringExtra("image")
        Glide.with(this).load("http://192.168.1.110:8080/Ecommerce/assets/images/" + txtimage).into(detailsImage)

        val sharedPreferences : SharedPreferences? = this.getSharedPreferences("checked", Context.MODE_PRIVATE)
        val cartCode = sharedPreferences?.getString("cartcode", "")
        btnAddtoCart2.setOnClickListener{
            progressDialog = indeterminateProgressDialog("Loading")
            progressDialog?.show()
            addTocart(id, cartCode!!)
        }
    }

    private fun addTocart(product_id: Int, cartCode: String){
        disposable.add(apiService.addToCart(product_id, cartCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> handle(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }

    private fun handle(response: Response){
        if (response.response) {
            setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
            startActivity<Cart>()
            finish()
        } else {
            showMessage("Product Already in the Cart")
        }
        progressDialog?.dismiss()
    }

    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {}
            }.show()
        }
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }


}
