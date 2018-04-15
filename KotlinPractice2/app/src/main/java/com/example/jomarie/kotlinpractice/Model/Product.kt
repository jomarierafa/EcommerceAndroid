package com.example.jomarie.kotlinpractice.Model



/**
 * Created by jomarie on 3/21/2018.
 */
data class Product(val id           : Int,
                   val productcode  : String,
                   val productname  : String,
                   val price        : Float,
                   val image        : String,
                   val description  : String)