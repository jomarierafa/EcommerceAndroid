package com.example.jomarie.kotlinpractice.Activity

import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.Model.Product
import com.example.jomarie.kotlinpractice.Model.User
import com.google.gson.annotations.SerializedName

/**
 * Created by jomarie on 3/21/2018.
 */
class ProductResponse {
    @SerializedName("product") // $response['product']
    var products: ArrayList<Product>? = null

    @SerializedName("cartproduct")
    var cartproducts: ArrayList<CartProduct>? = null

    @SerializedName("response")
    var response: Boolean? = null

    @SerializedName("response2")
    var response2: String? = null

    @SerializedName("userprofile")
    var userprofile: ArrayList<User>? = null

}