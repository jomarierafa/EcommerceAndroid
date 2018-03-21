package com.example.jomarie.kotlinpractice

import com.google.gson.annotations.SerializedName

/**
 * Created by jomarie on 3/21/2018.
 */
class ProductResponse {


    @SerializedName("product") // $response['product']
    var categories: ArrayList<ProductSerialized>? = null
}