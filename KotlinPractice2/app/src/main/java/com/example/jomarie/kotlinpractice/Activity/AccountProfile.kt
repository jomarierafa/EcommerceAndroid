package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_account_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AccountProfile : AppCompatActivity() {

    private val apiService by lazy {
        ApiInterface.create()
    }

    private var progressDialog      : ProgressDialog?    = null
    private var sharedPreferences   : SharedPreferences? = null
    private var PLACE_PICKER_REQUEST: Int                = 1
    private var id : Int = 0
    private var disposable = CompositeDisposable()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                val place   : Place = PlacePicker.getPlace(data, this)
                val address : String = String.format("%s", place.address)
                profaddressvalue.setText(address)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_profile)

        val linearlayout = findViewById<LinearLayout>(R.id.userInfoField)
        val txtname      = findViewById<TextView>(R.id.profnamevalue)
        val txtemail     = findViewById<TextView>(R.id.profemailvalue)
        val txtcontact   = findViewById<TextView>(R.id.profcontactvalue)
        val txtaddress   = findViewById<TextView>(R.id.profaddressvalue)

        id          = intent.getIntExtra("id", 0)
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

        Glide.with(this).load("http://192.168.1.110:8080/Ecommerce/assets/images/$image.jpg").apply(RequestOptions.circleCropTransform()).into(profilepic)


        updateProfile.setOnClickListener {
            if (updateProfile.text == "Update") {
                for (i in 0 until linearlayout.childCount) {
                    val child = linearlayout.getChildAt(i)
                    child.isEnabled = true
                }

                profaddressvalue.isEnabled = true
                editLocation.visibility = View.VISIBLE
                updateProfile.text = "Save"
            } else {
                for (i in 0 until linearlayout.childCount) {
                    val child = linearlayout.getChildAt(i)
                    child.isEnabled = false
                }

                profaddressvalue.isEnabled = false
                editLocation.visibility    = View.INVISIBLE
                headername.text            = txtname.text
                updateProfile.text         = "Update"
                progressDialog             = indeterminateProgressDialog("Updating profile...")
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

        editLocation.setOnClickListener{
            val builder : PlacePicker.IntentBuilder = PlacePicker.IntentBuilder()

            try {
                val intent = builder.build(this@AccountProfile)
                startActivityForResult(intent, PLACE_PICKER_REQUEST)
            }catch (e : GooglePlayServicesRepairableException){
                e.printStackTrace()
            } catch (e : GooglePlayServicesNotAvailableException){
                e.printStackTrace()
            }
        }
    }


    private fun update(id: Int, name: String, email : String, contact : String, address: String){
        disposable.add(apiService.updateProfile(id, name, email, contact, address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->toast(result.response2)
                            progressDialog?.dismiss()},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
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
            R.id.orders -> {
                startActivity<OderHistory>("user_id" to id)
                return true
            }
            else-> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }
}




