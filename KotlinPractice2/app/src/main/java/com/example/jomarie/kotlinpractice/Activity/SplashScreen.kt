package com.example.jomarie.kotlinpractice.Activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import com.example.jomarie.kotlinpractice.R
import org.jetbrains.anko.startActivity


class SplashScreen : Activity() {
    private val splash_screen = 4000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            startActivity<MainActivity>()
            finish()
        },splash_screen.toLong())

    }
}
