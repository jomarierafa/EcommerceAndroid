package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.User
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class Transaction : AppCompatActivity() {
    val apiService by lazy {
        ApiInterface.create()
    }

    var progressDialog  : ProgressDialog?   = null
    var txtname         : TextInputLayout?  = null
    var txtemail        : TextInputLayout?  = null
    var txtcontact      : TextInputLayout?  = null
    var txtaddress      : TextInputLayout?  = null
    var loginDialog     : DialogInterface?  = null

    private var mUserInfoList: java.util.ArrayList<User>?    = null
    private var sharedPreferences : SharedPreferences?       = null
    private var disposable : Disposable?                     = null
    var tf : Typeface? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        txtname    = findViewById<View>(R.id.costumername)    as TextInputLayout
        txtemail   = findViewById<View>(R.id.costumeremail)   as TextInputLayout
        txtcontact = findViewById<View>(R.id.costumercontact) as TextInputLayout
        txtaddress = findViewById<View>(R.id.costumeraddress) as TextInputLayout

        val name    = findViewById<View>(R.id.costumernamevalue)    as EditText
        val email   = findViewById<View>(R.id.costumeremailvalue)   as EditText
        val contact = findViewById<View>(R.id.costumercontactvalue) as EditText
        val address = findViewById<View>(R.id.costumeraddressvalue) as EditText

        val btnSave = findViewById<Button>(R.id.btnSaveTransac)
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

        //login button
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener{
            loginAlert()
        }

        tf = Typeface.createFromAsset(assets, "fonts/Shenanigans.ttf")
        btnSave.typeface  = tf
        btnLogin.typeface = tf
    }

    //saving Transaction
    fun addTransaction(name: String, email: String, contact: Int, address: String){
        disposable = apiService.saveTransaction(name, email, contact, address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> transactionResponse(result)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }
    private fun transactionResponse(response : Response) {
        showMessage("Order Successful")
        progressDialog?.dismiss()
    }

    //login
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
                addTransaction(user.name, user.email ,user.contact.toString().toInt(), user.address)
                val editor = sharedPreferences?.edit()
                editor?.putBoolean("LOGGED", true)
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

    //Dialogs
    fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    finish()
                }
            }.show()
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
                        inputType   = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
                    }.lparams{ width = matchParent }

                    textView("Don't Have An Account?"){
                        textColor = Color.BLUE
                        onClick {
                            startActivity<Register>()
                        }
                    }
                    button("Login"){
                        typeface = tf
                        onClick{
                            if(username.text.isEmpty() || password.text.isEmpty()){
                                toast("All fields are required")
                            }else{
                                progressDialog = indeterminateProgressDialog("Logging in..")
                                progressDialog!!.show()
                                login(username.text.toString(), password.text.toString())
                                this@Transaction.loginDialog?.dismiss()
                            }
                        }
                    }.lparams{width = matchParent }
                }
            }
        }.show()
    }

    //input validations
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
