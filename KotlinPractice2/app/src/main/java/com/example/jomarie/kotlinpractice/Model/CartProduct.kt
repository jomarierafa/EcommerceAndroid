package com.example.jomarie.kotlinpractice.Model

import com.google.gson.annotations.SerializedName

/**
 * Created by jomarie on 3/31/2018.
 */
class CartProduct{
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("code")
    var code: String? = null

    @SerializedName("product")
    var productname: String? = null

    @SerializedName("qty")
    var qty: Int = 0

    @SerializedName("stock")
    var stock: Int = 0

    @SerializedName("amount")
    var amount: Int = 0

    @SerializedName("totalamount")
    var totalamount: Int = 0

    @SerializedName("image")
    var image: String? = null
}