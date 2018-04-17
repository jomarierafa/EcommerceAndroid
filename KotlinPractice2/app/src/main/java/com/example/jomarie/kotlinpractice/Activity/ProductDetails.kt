package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_product_details.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


class ProductDetails : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    var disposable      : Disposable?     =  null
    var progressDialog  : ProgressDialog? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        detailsProduct.text     = intent.getStringExtra("product")
        detailsPrice.text       = "$ " + intent.getFloatExtra("price", 0F).toString()
        detailsDescription.text = intent.getStringExtra("description")

        val id       = intent.getIntExtra("id", 0)
        val txtimage = intent.getStringExtra("image")
        Picasso.with(this).load("http://192.168.1.124:8080/Ecommerce/assets/images/" + txtimage).into(detailsImage)

        val sharedPreferences : SharedPreferences? = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        val user_id = sharedPreferences?.getInt("id", 0)
        btnAddtoCart2.setOnClickListener{
            progressDialog = indeterminateProgressDialog("Loading")
            progressDialog?.show()
            addTocart(id, user_id!!)
        }
    }

    private fun addTocart(product_id: Int, user_id : Int){
        disposable = apiService.addToCart(product_id, user_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> handle(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    private fun handle(response: Response){
        if (response.response) {
            showMessage("Product Added Successfuly")
        } else {
            showMessage("Product Already in the Cart")
        }
        progressDialog?.dismiss()
        setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
    }

    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {}
            }.show()
        }
    }


}
