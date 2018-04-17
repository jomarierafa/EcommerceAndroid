package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_account_profile.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class AccountProfile : AppCompatActivity() {

    private val apiService by lazy {
        ApiInterface.create()
    }

    private var disposable          : Disposable?        = null
    private var progressDialog      : ProgressDialog?    = null
    private var sharedPreferences   : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_profile)

        val linearlayout = findViewById<LinearLayout>(R.id.userInfoField)
        val txtname      = findViewById<TextView>(R.id.profnamevalue)
        val txtemail     = findViewById<TextView>(R.id.profemailvalue)
        val txtcontact   = findViewById<TextView>(R.id.profcontactvalue)
        val txtaddress   = findViewById<TextView>(R.id.profaddressvalue)

        val id      = intent.getIntExtra("id", 0)
        val name    = intent.getStringExtra("name")
        val email   = intent.getStringExtra("email")
        val contact = intent.getStringExtra("contact")
        val address = intent.getStringExtra("address")
        val image   = intent.getStringExtra("image")

        headername.text = name
        txtname.text    = name
        txtemail.text   = email
        txtcontact.text = contact
        txtaddress.text = address

        Picasso.with(this).load("http://192.168.1.124:8080/Ecommerce/assets/images/$image.jpg").into(profilepic)

        val btnUpdateProfile = findViewById<Button>(R.id.updateProfile)
        btnUpdateProfile.setOnClickListener {
            if (btnUpdateProfile.text == "Update") {
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
                update(id, txtname.text.toString(), txtemail.text.toString(), txtcontact.text.toString(), txtaddress.text.toString())

                val sharedPreferences = this@AccountProfile.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()
                editor?.putBoolean("LOGGED", true)
                editor?.putString("name", txtname.text.toString())
                editor?.putString("email", txtemail.text.toString())
                editor?.putString("contact", txtcontact.text.toString())
                editor?.putString("address", txtaddress.text.toString())
                editor?.apply()

            }
        }
    }


    private fun update(id: Int, name: String, email : String, contact : String, address: String){
        disposable = apiService.updateProfile(id, name, email, contact, address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->toast(result.response2)
                                progressDialog?.dismiss()},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.logout -> {
                sharedPreferences   = this.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
                sharedPreferences!!.edit().clear().apply()
                setResult(Activity.RESULT_OK, intent.putExtra("msg", "switch"))
                finish()
                return true
            }
            else-> return super.onOptionsItemSelected(item)
        }
    }
}




