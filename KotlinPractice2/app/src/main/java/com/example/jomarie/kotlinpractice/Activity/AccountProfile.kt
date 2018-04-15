package com.example.jomarie.kotlinpractice.Activity

import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_account_profile.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class AccountProfile : AppCompatActivity() {

    val apiService by lazy {
        ApiInterface.create()
    }

    var disposable      : Disposable?  = null
    var progressDialog  : ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_profile)

        val linearlayout = findViewById<LinearLayout>(R.id.userInfoField)
        val headername   = findViewById<TextView>(R.id.headername)
        val txtname      = findViewById<TextView>(R.id.profnamevalue)
        val txtemail     = findViewById<TextView>(R.id.profemailvalue)
        val txtcontact   = findViewById<TextView>(R.id.profcontactvalue)
        val txtaddress   = findViewById<TextView>(R.id.profaddressvalue)

        val id      = intent.getIntExtra("id", 0)
        val name    = intent.getStringExtra("name")
        val email   = intent.getStringExtra("email")
        val contact = intent.getIntExtra("contact", 0).toString()
        val address = intent.getStringExtra("address")

        headername.text = name
        txtname.text    = name
        txtemail.text   = email
        txtcontact.text = contact
        txtaddress.text = address

        val btnUpdateProfile = findViewById<Button>(R.id.updateProfile)
        btnUpdateProfile.setOnClickListener {
            if (btnUpdateProfile.text.equals("Update")) {
                for (i in 0 until linearlayout.childCount) {
                    val child = linearlayout.getChildAt(i)
                    child.isEnabled = true
                }

                btnUpdateProfile.text = "Save"
            } else {
                for (i in 0 until linearlayout.childCount) {
                    val child = linearlayout.getChildAt(i)
                    child.isEnabled = false
                }

                headername.text       = txtname.text
                btnUpdateProfile.text = "Update"
                progressDialog = indeterminateProgressDialog("Updating profile...")
                progressDialog?.setCancelable(false)
                progressDialog?.show()
                update(id, txtname.text.toString(), txtemail.text.toString(), txtcontact.text.toString().toInt(), txtaddress.text.toString())

                val sharedPreferences = this@AccountProfile.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()
                editor?.putBoolean("LOGGED", true)
                editor?.putString("name", txtname.text.toString())
                editor?.putString("email", txtemail.text.toString())
                editor?.putInt("contact", txtcontact.text.toString().toInt())
                editor?.putString("address", txtaddress.text.toString())
                editor?.apply()

            }
        }
    }


    fun update(id: Int, name: String, email : String, contact : Int, address: String){
        disposable = apiService.updateProfile(id, name, email, contact, address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->toast(result.response2)
                                progressDialog?.dismiss()},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }


}




