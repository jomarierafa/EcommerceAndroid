package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
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
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


class ProductDetails : AppCompatActivity() {
    val apiService by lazy {
        ApiInterface.create()
    }

    var disposable      : Disposable?     =  null
    var progressDialog  : ProgressDialog? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val productname = findViewById<TextView>(R.id.detailsProduct)
        val price       = findViewById<TextView>(R.id.detailsPrice)
        val imageView   = findViewById<ImageView>(R.id.detailsImage)
        val description = findViewById<TextView>(R.id.textView4)

        productname.text = intent.getStringExtra("product")
        price.text       = "$ " + intent.getFloatExtra("price", 0F).toString()
        description.text = intent.getStringExtra("description")

        val id       = intent.getIntExtra("id", 0)
        val txtimage = intent.getStringExtra("image")
        Picasso.with(this).load("http://192.168.254.101:8080/Ecommerce/assets/images/" + txtimage).into(imageView)

        val btnAdd = findViewById<Button>(R.id.btnAddtoCart2)
        btnAdd.setOnClickListener{
            progressDialog = indeterminateProgressDialog("Loading")
            progressDialog?.show()
            addTocart(id)
        }
    }

    fun addTocart(product_id: Int){
        disposable = apiService.addToCart(product_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> handle(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    fun handle(response: Response){
        if (response.response) {
            showMessage("Product Added Successfuly")
        } else {
            showMessage("Product Already in the Cart")
        }
        progressDialog?.dismiss()
    }

    fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {}
            }.show()
        }
    }

}
