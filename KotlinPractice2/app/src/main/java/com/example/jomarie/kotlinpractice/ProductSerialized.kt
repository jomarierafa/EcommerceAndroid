package com.example.jomarie.kotlinpractice

import com.google.gson.annotations.SerializedName

/**
 * Created by jomarie on 3/21/2018.
 */
class ProductSerialized {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("productcode")
    var productcode: String? = null

    @SerializedName("productname")
    var productname: String? = null

    @SerializedName("quantity_output")
    var quantity_output: Int = 0

    @SerializedName("price_output")
    var price_output: String? = null
}