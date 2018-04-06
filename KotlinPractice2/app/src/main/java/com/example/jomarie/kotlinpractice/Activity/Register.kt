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
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Register : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null

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

    fun registerUser(name: String, username: String, password: String, email: String, contact: Int, address: String ){
        val apiService  = ApiInterface.create()
        val call = apiService.registerUser(name,username,password,email,contact,address)
        call.enqueue(object : Callback<ProductResponse>{
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable?) {
                    showMessage("Something went Wrong!!")
            }

            override fun onResponse(call: Call<ProductResponse>?, response: Response<ProductResponse>?) {
                if (response != null) {
                    progressDialog!!.dismiss()
                    showMessage(response.body().response2!!)
                }
            }

        })
    }

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
