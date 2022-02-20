package com.raju.disney.base

import androidx.appcompat.app.AppCompatActivity
import com.raju.disney.opentelemetry.DisneyOtel

abstract class BaseActivity : AppCompatActivity() {
    val otel = DisneyOtel.getInstance()
}
