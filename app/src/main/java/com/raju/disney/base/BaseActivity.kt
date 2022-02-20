package com.raju.disney.base

import androidx.appcompat.app.AppCompatActivity
import com.raju.disney.opentelemetry.DisneyOTel

abstract class BaseActivity : AppCompatActivity() {
    val oTel = DisneyOTel.instance
}
