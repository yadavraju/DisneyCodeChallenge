package com.raju.disney.ui.activity

import TOAST
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.raju.disney.R
import com.raju.disney.base.BaseActivity
import com.raju.disney.data.FlightData
import com.raju.disney.opentelemetry.OtelConfiguration
import com.raju.disney.opentelemetry.OtelConfiguration.createChildSpan
import com.raju.disney.opentelemetry.OtelConfiguration.createSpan
import com.raju.disney.ui.activity.viewmodel.FlightActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.opentelemetry.api.trace.Tracer
import kotlinx.android.synthetic.main.activity_flight.*


@AndroidEntryPoint
class FlightActivity : BaseActivity() {

    private val tracer: Tracer = OtelConfiguration.getTracer("FlightActivity")
    private val parentSpan = tracer.createSpan("FlightActivity:api:request")

    private val viewModel: FlightActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight)
        Log.e("Raju", "TraceId: Flight " + parentSpan.spanContext.traceId)
        Log.e("Raju", "spanId: " + parentSpan.spanContext.spanId)
        try {
            parentSpan.makeCurrent().use {
                viewModel.fetchFlightData()
                viewModel.displayFlightData.observeEvent(this, this::showUser)
                viewModel.showErrorMessage.observeEvent(this, this::showErrorMessage)
            }
        } finally {
            parentSpan.end()
        }
    }

    private fun showUser(flight: FlightData) {
        val span = tracer.createChildSpan("setDataTo:ui", parentSpan)
        try {
            span.makeCurrent().use {
                val result = StringBuilder()
                flight.forEach {
                    result.append(it).append("\n\n")
                }
                tvFlightData.text = result
            }
        } finally {
            span.end()
        }
    }


    private fun showErrorMessage(message: String?) {
        TOAST(message)
    }
}