package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_transaction.*
import org.jetbrains.anko.*
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.sdk25.coroutines.onClick

class Transaction : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var progressDialog      : ProgressDialog?   = null
    private var mUserInfoList       : java.util.ArrayList<User>?    = null
    private var sharedPreferences   : SharedPreferences?            = null
    private var user_id             : Int                           = 0

    private var disposable = CompositeDisposable()
    private var totalamount : String?   = null
    private var quantityitem: Int       = 0
    private val PLACE_PICKER_REQUEST    = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        sharedPreferences = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        user_id = sharedPreferences?.getInt("id", 0)!!

        quantityitem    = intent.getIntExtra("itemQuantity", 0)
        totalamount     = intent.getStringExtra("totalAmount")

        btnProceedPay.setOnClickListener {
            if (
                    validateName(costumernamevalue.text.toString()) &&
                    validateEmail(costumeremailvalue.text.toString()) &&
                    validateMobile(costumercontactvalue.text.toString()) &&
                    validateAddress(costumeraddressvalue.text.toString())
                    ) {

                startActivityForResult<PaymentActivity>(30,
                                        "user_id" to user_id.toString(),
                                                "name" to costumernamevalue.text.toString(),
                                                "email" to costumeremailvalue.text.toString(),
                                                "contact" to costumercontactvalue.text.toString(),
                                                "address" to costumeraddressvalue.text.toString(),
                                                "items" to quantityitem,
                                                "totalamount" to totalamount.toString())
            }
        }

        //login button
        btnLogin.setOnClickListener{
            loginAlert()
        }

        addLocation.setOnClickListener {
            val builder : PlacePicker.IntentBuilder = PlacePicker.IntentBuilder()

            try {
                val intent = builder.build(this@Transaction)
                startActivityForResult(intent, PLACE_PICKER_REQUEST)
            }catch (e : GooglePlayServicesRepairableException){
                e.printStackTrace()
            } catch (e : GooglePlayServicesNotAvailableException){
                e.printStackTrace()
            }
        }

    }

    //login
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

                startActivityForResult<PaymentActivity>(30,"user_id" to user.id,
                        "name" to user.name,
                        "email" to user.email,
                        "contact" to user.contact,
                        "address" to user.address,
                        "items" to quantityitem,
                        "totalamount" to totalamount.toString())
            }
        }else{
            longToast("Incorrect Username or Password")
        }
        progressDialog?.dismiss()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                val place   : Place = PlacePicker.getPlace(data, this)
                val address : String = String.format("%s", place.address)
                costumeraddressvalue.setText(address)
            }
        }
        if(requestCode == 30 && data != null) {
            setResult(Activity.RESULT_OK, intent.putExtra("msg", "loadcounter"))
            finish()
        }
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }


}
