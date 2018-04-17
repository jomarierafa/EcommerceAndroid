package com.example.jomarie.kotlinpractice.Activity

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.Model.User
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_transaction.*
import org.jetbrains.anko.*
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.sdk25.coroutines.onClick

class Transaction : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var progressDialog  : ProgressDialog?   = null
    private var paymentDialog   : DialogInterface?  = null

    private var mUserInfoList       : java.util.ArrayList<User>?    = null
    private var sharedPreferences   : SharedPreferences?            = null
    private var disposable          : Disposable?                   = null
    private var user_id             : Int                           = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        user_id = sharedPreferences?.getInt("id", 0)!!

        btnSaveTransac.setOnClickListener {
            if (
                    validateName(costumernamevalue.text.toString()) &&
                    validateEmail(costumeremailvalue.text.toString()) &&
                    validateMobile(costumercontactvalue.text.toString()) &&
                    validateAddress(costumeraddressvalue.text.toString())
                    ) {
                progressDialog = indeterminateProgressDialog("Saving....")
                progressDialog!!.show()
                addTransaction( costumernamevalue.text.toString(),
                                costumeremailvalue.text.toString(),
                                costumercontactvalue.text.toString(),
                                costumeraddressvalue.text.toString(),
                                cardnovalue.text.toString().toInt(),
                                expiryvalue.text.toString(),
                                cvcvalue.text.toString().toInt())
            }
        }

        //login button
        btnLogin.setOnClickListener{
            loginAlert()
        }

    }

    //saving Transaction
    private fun addTransaction(name: String, email: String, contact: String, address: String, cardno: Int, expiry: String, cvccode: Int){
        disposable = apiService.saveTransaction(user_id, name, email, contact, address, cardno, expiry, cvccode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->
                            progressDialog?.dismiss()
                            showMessage("Order Successful")},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }
    //login
    private fun login(username : String, password: String){
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
                editor?.putString("name", user.name)
                editor?.putString("email", user.email)
                editor?.putString("contact", user.contact)
                editor?.putString("address", user.address)
                editor?.putString("image", user.image)
                editor?.apply()
                showDetails(user.name, user.email,user.contact,user.address)
            }
        }else{
            longToast("Incorrect Username or Password")
        }
        progressDialog?.dismiss()
    }

    //Dialogs
    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    finish()
                }
            }.show()
        }
    }

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
                if(username.text.isEmpty() || password.text.isEmpty()){
                    toast("All fields are required")
                }else{
                    progressDialog = indeterminateProgressDialog("Logging in..")
                    progressDialog!!.show()
                    login(username.text.toString(), password.text.toString())
                    dialog.dismiss()
                }
            }
        }

        val signUp = view.findViewById<TextView>(R.id.signup)
        signUp.setOnClickListener {
            startActivity<Register>()
        }

        dialog.show()
    }

    private fun showDetails(name: String, email : String, contact : String, address: String){
        this.paymentDialog = alert {
            isCancelable = false
            title = "Payment Details"
            customView {
                verticalLayout {
                    padding = dip(15)
                    val cardno = editText{
                        hint     = "card no."
                        textSize = sp(10).toFloat()
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }.lparams{ width = matchParent }
                    val expiry = editText{
                        hint      = "Expiry"
                        textSize  = sp(10).toFloat()
                    }.lparams{ width = matchParent }
                    val cvccode = editText {
                        hint      = "CVC Code"
                        textSize  = sp(10).toFloat()
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }.lparams{ width = matchParent }

                    button("Submit"){
                        onClick{
                            progressDialog = indeterminateProgressDialog("Saving...")
                            progressDialog!!.show()
                            addTransaction(name,email,contact,address,cardno.text.toString().toInt(),expiry.text.toString(),cvccode.text.toString().toInt())
                            this@Transaction.paymentDialog?.dismiss()
                        }
                    }.lparams{width = matchParent }
                    button("Switch Account"){
                        onClick{
                            sharedPreferences!!.edit().clear().apply()
                            this@Transaction.paymentDialog?.dismiss()
                            loginAlert()
                        }
                    }.lparams{width = matchParent }


                }
            }
        }.show()
    }

    //input validations
    private fun validateName(string: String): Boolean {
        if (string.isEmpty()) {
            costumername!!.error = "Enter Your Name"
            return false
        } else if (string.length > 50) {
            costumername!!.error = "Maximum 50 Characters"
            return false
        }
        costumername!!.isErrorEnabled = false
        return true
    }
    private fun validateEmail(string: String): Boolean{
        if (string.isEmpty()) {
            costumeremail!!.error = "Enter Your Email Address"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(string).matches()) {
            costumeremail!!.error = "Enter A Valid Email Address"
            return false
        }
        costumeremail!!.isErrorEnabled = false
        return true
    }
    private fun validateMobile(string: String): Boolean{
        if (string.isEmpty()) {
            costumercontact!!.error = "Enter Your Mobile Number"
            return false
        }
        if (string.length != 10) {
            costumercontact!!.error = "Enter A Valid Mobile Number"
            return false
        }
        costumercontact!!.isErrorEnabled = false
        return true
    }
    private fun validateAddress(string: String): Boolean{
        if(string.isEmpty()){
            costumeraddress!!.error = "Enter Address"
            return false
        }
        costumeraddress!!.isErrorEnabled = false
        return true;
    }



}
