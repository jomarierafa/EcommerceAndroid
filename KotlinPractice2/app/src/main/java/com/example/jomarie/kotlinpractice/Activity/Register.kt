package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.View
import android.widget.EditText
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class Register : AppCompatActivity() {
    val apiService by lazy {
        ApiInterface.create()
    }

    var progressDialog  : ProgressDialog? = null
    var disposable      : Disposable?        = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name     = findViewById<View>(R.id.regnamevalue)     as EditText
        val user     = findViewById<View>(R.id.regusernamevalue) as EditText
        val pass     = findViewById<View>(R.id.regpasswordvalue) as EditText
        val email    = findViewById<View>(R.id.regemailvalue)    as EditText
        val contact  = findViewById<View>(R.id.regcontactvalue)  as EditText
        val address  = findViewById<View>(R.id.regaddressvalue)  as EditText

        val btnRegister = findViewById<View>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            progressDialog = indeterminateProgressDialog("Loading ...")
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            registerUser(name.text.toString(),user.text.toString(),pass.text.toString(),email.text.toString(),contact.text.toString().toInt(),address.text.toString())
        }
    }

    //register User
    fun registerUser(name: String, username: String, password: String, email: String, contact: Int, address: String){
        disposable = apiService.registerUser(name, username, password, email, contact, address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> registerResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }
    fun registerResponse(response : Response){
        progressDialog!!.dismiss()
        showMessage(response.response2)
    }

    //alertdialog
    fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    finish()
                }
            }.show()
        }
    }
}
