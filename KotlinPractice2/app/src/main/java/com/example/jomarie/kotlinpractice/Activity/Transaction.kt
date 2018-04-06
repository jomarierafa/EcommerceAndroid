package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.Patterns
import android.view.View
import android.widget.EditText
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Transaction : AppCompatActivity() {
    var progressDialog  : ProgressDialog?   = null
    var txtname         : TextInputLayout?  = null
    var txtemail        : TextInputLayout?  = null
    var txtcontact      : TextInputLayout?  = null
    var txtaddress      : TextInputLayout?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        txtname    = findViewById<View>(R.id.costumername) as TextInputLayout
        txtemail   = findViewById<View>(R.id.costumeremail) as TextInputLayout
        txtcontact = findViewById<View>(R.id.costumercontact) as TextInputLayout
        txtaddress = findViewById<View>(R.id.costumeraddress) as TextInputLayout

        val name    = findViewById<View>(R.id.costumernamevalue) as EditText
        val email   = findViewById<View>(R.id.costumeremailvalue) as EditText
        val contact = findViewById<View>(R.id.costumercontactvalue) as EditText
        val address = findViewById<View>(R.id.costumeraddressvalue) as EditText

        val btnSave = findViewById<View>(R.id.btnSaveTransac)
        btnSave.setOnClickListener {
            if (
                    validateName(name.text.toString()) &&
                    validateEmail(email.text.toString()) &&
                    validateMobile(contact.text.toString()) &&
                    validateAddress(contact.text.toString())
                    ) {
                progressDialog = indeterminateProgressDialog("Saving....")
                progressDialog!!.show()
                addTransaction(name.text.toString(), email.text.toString(), contact.text.toString().toInt(), address.text.toString())
            }
        }

        val btnLogin = findViewById<View>(R.id.btnLogin)
        btnLogin.setOnClickListener{
            loginAlert()
        }
    }

    fun addTransaction(name: String, email: String, contact: Int, address: String){
        val apiService = ApiInterface.create()
        val call = apiService.saveTransaction(name,email,contact,address)
        call.enqueue(object: Callback<ProductResponse>{
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable?) {
                showMessage("Failed")
            }

            override fun onResponse(call: Call<ProductResponse>?, response: Response<ProductResponse>?) {
                showMessage("Order Successfuly")
                progressDialog!!.dismiss()

            }

        })
    }

    fun login(username : String, password: String){
        val apiService = ApiInterface.create()
        val call = apiService.login(username, password)
        call.enqueue(object : Callback<ProductResponse>{
            override fun onFailure(call: Call<ProductResponse>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<ProductResponse>?, response: Response<ProductResponse>?) {
                if(response?.body()!!.response!!){

                }else{
                    toast(response?.body()!!.response!!.toString())
                }
            }

        })
    }

    fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    val intent = Intent(this@Transaction, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.show()
        }
    }

    fun loginAlert(){
        alert {
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
                        inputType   = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
                    }.lparams{ width = matchParent }

                    textView("Dont Have An Account?"){
                        textColor = Color.BLUE
                        onClick {
                            val intent = Intent(this@Transaction, Register::class.java)
                            startActivity(intent)
                        }
                    }
                    button("Login"){
                        onClick{
                            login(username.text.toString(), password.text.toString())
                            longToast("" + username.text + password.text)
                        }
                    }.lparams{width = matchParent }
                }
            }
        }.show()
    }

    private fun validateName(string: String): Boolean {
        if (string.isEmpty()) {
            txtname!!.error = "Enter Your Name"
            return false
        } else if (string.length > 50) {
            txtname!!.error = "Maximum 50 Characters"
            return false
        }
        txtname!!.isErrorEnabled = false
        return true
    }

    private fun validateEmail(string: String): Boolean{
        if (string.isEmpty()) {
            txtemail!!.error = "Enter Your Email Address"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(string).matches()) {
            txtemail!!.error = "Enter A Valid Email Address"
            return false
        }
        txtemail!!.isErrorEnabled = false
        return true
    }

    private fun validateMobile(string: String): Boolean{
        if (string.isEmpty()) {
            txtcontact!!.error = "Enter Your Mobile Number"
            return false
        }
        if (string.length != 10) {
            txtcontact!!.error = "Enter A Valid Mobile Number"
            return false
        }
        txtcontact!!.isErrorEnabled = false
        return true
    }

    private fun validateAddress(string: String): Boolean{
        if(string.isEmpty()){
            txtaddress!!.error = "Enter Address"
            return false
        }
        txtaddress!!.isErrorEnabled = false
        return true;
    }



}
