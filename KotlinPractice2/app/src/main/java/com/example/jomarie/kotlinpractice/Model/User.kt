package com.example.jomarie.kotlinpractice.Model


/**
 * Created by jomarie on 4/6/2018.
 */
data class User (
    val id      : Int,
    var name    : String,
    var username: String,
    var password: String,
    var email   : String,
    var contact : String,
    var address : String,
    var image   : String
)