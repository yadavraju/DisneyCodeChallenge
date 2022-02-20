package com.raju.disney.ui.activity

import android.content.Intent
import android.os.Bundle
import com.raju.disney.R
import com.raju.disney.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_dahboard.*

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dahboard)
        btnTraceFlight.setOnClickListener {
            val span = otel.startWorkflow("open:instrumented:api")
            startActivity(Intent(this, FlightActivity::class.java))
            span.end()
        }
        btnTraceMarvelApi.setOnClickListener {
            val span = otel.startWorkflow("open:not:instrumented:api")
            startActivity(Intent(this, MainActivity::class.java))
            span.end()
        }
    }
}