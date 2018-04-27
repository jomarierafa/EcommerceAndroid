package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.EditText
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_oder_history.*
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class Register : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }
    private var disposable = CompositeDisposable()
    private var progressDialog          : ProgressDialog?    = null
    private var bitmap                  : Bitmap?            = null
    private val PLACE_PICKER_REQUEST    : Int                = 1
    private var IMAGE_STATUS            : Boolean            = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name     = findViewById<View>(R.id.regnamevalue)     as EditText
        val user     = findViewById<View>(R.id.regusernamevalue) as EditText
        val pass     = findViewById<View>(R.id.regpasswordvalue) as EditText
        val email    = findViewById<View>(R.id.regemailvalue)    as EditText
        val contact  = findViewById<View>(R.id.regcontactvalue)  as EditText
        val address  = findViewById<View>(R.id.regaddressvalue)  as EditText

        btnRegister.setOnClickListener {
            val image : String = imageToString()
            if(validateName(regnamevalue.text.toString()) &&
                    validateUsername(regusernamevalue.text.toString()) &&
                    validatePassword(regpasswordvalue.text.toString()) &&
                    validateConfirm(confirmpassvalue.text.toString()) &&
                    validateEmail(regemailvalue.text.toString()) &&
                    validateMobile(regcontactvalue.text.toString()) &&
                    validateAddress(regaddressvalue.text.toString()) &&
                    validateProfile()
                    ) {
                progressDialog = indeterminateProgressDialog("Loading ...")
                progressDialog!!.setCancelable(false)
                progressDialog!!.show()
                registerUser(name.text.toString(),user.text.toString(),pass.text.toString(),email.text.toString(),contact.text.toString(),address.text.toString(), image)
            }


        }

        //upload profile picture
        userprofile.setOnClickListener{
            selectImage()
        }

        //select address
        reglocation.setOnClickListener {
            val builder : PlacePicker.IntentBuilder = PlacePicker.IntentBuilder()

            try {
                val intent = builder.build(this@Register)
                startActivityForResult(intent, PLACE_PICKER_REQUEST)
            }catch (e : GooglePlayServicesRepairableException){
                e.printStackTrace()
            } catch (e : GooglePlayServicesNotAvailableException){
                e.printStackTrace()
            }
        }
    }

    //register User
    private fun registerUser(name: String, username: String, password: String, email: String, contact: String, address: String, image : String){
        disposable.add(apiService.registerUser(name, username, password, email, contact, address, image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> progressDialog!!.dismiss()
                                showMessage(result.response2)},
                        {error->  progressDialog!!.dismiss()
                                toast("Error ${error.localizedMessage}")}
                ))
    }

    //alertdialog
    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    if(message == "Register Successful"){
                        finish()
                    }
                }
            }.show()
        }
    }

    private fun selectImage(){
        IMAGE_STATUS = true
        val intent =  Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 777)
    }
    private fun imageToString() : String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
        val imgByte : ByteArray? = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imgByte, Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 777 && resultCode == RESULT_OK && data != null){
            val path : Uri? = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, path)
                userprofile.setImageBitmap(bitmap)
            }catch (e : IOException){
                e.printStackTrace()
            }
        }

        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                val place   : Place = PlacePicker.getPlace(data, this)
                val address : String = String.format("%s", place.address)
                regaddressvalue.setText(address)
            }
        }
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    //validations
    private fun validateName(string: String): Boolean {
        if (string.isEmpty()) {
            regname.error = "Enter Your Name"
            return false
        } else if (string.length > 50) {
            regname.error = "Maximum 50 Characters"
            return false
        }
        regname.isErrorEnabled = false
        return true
    }
    private fun validateUsername(string: String): Boolean {
        if (string == "") {
            regusername.error = "Enter A Username"
            return false
        } else if (string.length > 50) {
            regusername.error = "Maximum 50 Characters"
            return false
        } else if (string.length < 6) {
            regusername.error = "Minimum 6 Characters"
            return false
        }
        regusername.isErrorEnabled = false
        return true
    }
    private fun validatePassword(string: String): Boolean {
        if (string == "") {
            regpassword.error = "Enter Your Password"
            return false
        } else if (string.length > 32) {
            regpassword.error = "Maximum 32 Characters"
            return false
        } else if (string.length < 8) {
            regpassword.error = "Minimum 8 Characters"
            return false
        }
        regpassword.isErrorEnabled = false
        return true
    }
    private fun validateConfirm(string: String): Boolean {
        if (string == "") {
            confirmpassword.error = "Re-Enter Your Password"
            return false
        } else if (string != regpassword.editText?.text.toString()) {
            confirmpassword.error = "Passwords Do Not Match"
            regpassword.error = "Passwords Do Not Match"
            return false
        }
        confirmpassword.isErrorEnabled = false
        return true
    }
    private fun validateEmail(string: String): Boolean{
        if (string.isEmpty()) {
            regemail.error = "Enter Your Email Address"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(string).matches()) {
            regemail.error = "Enter A Valid Email Address"
            return false
        }
        regemail.isErrorEnabled = false
        return true
    }
    private fun validateMobile(string: String): Boolean{
        if (string.isEmpty()) {
            regcontact.error = "Enter Your Mobile Number"
            return false
        }
        if (string.length != 10) {
            regcontact!!.error = "Enter A Valid Mobile Number"
            return false
        }
        regcontact.isErrorEnabled = false
        return true
    }
    private fun validateAddress(string: String): Boolean{
        if(string.isEmpty()){
            regaddress.error = "Enter Address"
            return false
        }
        regaddress.isErrorEnabled = false
        return true;
    }
    private fun validateProfile() : Boolean {
        if (!IMAGE_STATUS)
            toast("Select A Profile Picture")
        return IMAGE_STATUS;
    }




}
