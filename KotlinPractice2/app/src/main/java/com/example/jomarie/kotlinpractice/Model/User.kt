package com.example.jomarie.kotlinpractice.Model

import com.google.gson.annotations.SerializedName

/**
 * Created by jomarie on 4/6/2018.
 */
class User {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("name")
    var name: String? = null

    @SerializedName("username")
    var username: String? = null

    @SerializedName("password")
    var password: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("contact")
    var contact: Int = 0

    @SerializedName("address")
    var address: String? = null
}