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
import android.view.View
import android.widget.EditText
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class Register : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var progressDialog  : ProgressDialog?    = null
    private var disposable      : Disposable?        = null
    private var bitmap          : Bitmap?            = null

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

            val image : String = imageToString()
            registerUser(name.text.toString(),user.text.toString(),pass.text.toString(),email.text.toString(),contact.text.toString().toInt(),address.text.toString(), image)
        }

        userprofile.setOnClickListener{
            selectImage()
        }
    }

    //register User
    private fun registerUser(name: String, username: String, password: String, email: String, contact: Int, address: String, image : String){
        disposable = apiService.registerUser(name, username, password, email, contact, address, image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result-> progressDialog!!.dismiss()
                                showMessage(result.response2)},
                        {error-> toast("Error ${error.localizedMessage}")}
                )
    }

    //alertdialog
    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    finish()
                }
            }.show()
        }
    }

    private fun selectImage(){
        val intent =  Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 777)

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
    }

    private fun imageToString() : String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
        val imgByte : ByteArray? = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imgByte, Base64.DEFAULT)
    }
}
