package com.leopardcat.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, FloatingService::class.java))
        finish()
    }
}
