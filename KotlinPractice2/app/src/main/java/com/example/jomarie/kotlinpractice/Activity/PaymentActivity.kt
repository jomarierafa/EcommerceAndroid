package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.jomarie.kotlinpractice.ApiInterface
import com.example.jomarie.kotlinpractice.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_payment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


class PaymentActivity : AppCompatActivity() {
    private val apiService by lazy {
        ApiInterface.create()
    }

    private var disposable = CompositeDisposable()
    private var progressDialog      : ProgressDialog?     = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val user_id = intent.getIntExtra("user_id", 0)
        val name    = intent.getStringExtra("name")
        val email   = intent.getStringExtra("email")
        val contact = intent.getStringExtra("email")
        val address = intent.getStringExtra("address")
        val items       = intent.getIntExtra("items", 0)
        val totalAmount = intent.getStringExtra("totalamount")

        item.text       = "Subtotal ($items item/s)"
        subtotal.text   = "$ $totalAmount"
        total.text      = "$ $totalAmount"

        val settings : SharedPreferences = getSharedPreferences("checked", 0)
        val cartCode = settings.getString("cartcode", "")

        btnSaveTransac.setOnClickListener {
            progressDialog = indeterminateProgressDialog("Placing Order....")
            progressDialog!!.show()
            addTransaction(user_id, cartCode, name, email, contact, address, cardnovalue.text.toString().toInt(), expiryvalue.text.toString(), cvcvalue.text.toString().toInt(),items, totalAmount.toString())
        }

    }

    //saving Transaction
    private fun addTransaction(user_id: Int, cartCode: String, name: String, email: String, contact: String, address: String, cardno: Int, expiry: String, cvvcode: Int, itemQuantity: Int, totalAmount: String){
        disposable.add(apiService.saveTransaction(user_id, cartCode, name, email, contact, address, cardno, expiry, cvvcode, itemQuantity, totalAmount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result->
                            progressDialog?.dismiss()
                            showMessage("Order Successful")},
                        {error-> toast("Error ${error.localizedMessage}")}
                ))
    }

    private fun showMessage(message: String){
        alert{
            alert(message) {
                yesButton {
                    setResult(Activity.RESULT_OK, intent.putExtra("msg", "ordersuccessful"))
                    finish()
                }
            }.show()
        }
    }

    private fun validateCVV(string: String): Boolean{
        if (string.isEmpty()) {
            cvc.error = "Enter your CVV code"
            return false
        }
        if (string.length < 3) {
            cvc.error = "CVV is 3 digits"
            return false
        }
        cvc!!.isErrorEnabled = false
        return true
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }


}
