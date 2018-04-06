package com.example.jomarie.kotlinpractice.Model

import com.google.gson.annotations.SerializedName

/**
 * Created by jomarie on 3/21/2018.
 */
class Product {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("productcode")
    var productcode: String? = null

    @SerializedName("productname")
    var productname: String? = null

    @SerializedName("price")
    var price: String? = null

    @SerializedName("image")
    var image: String? = null
}