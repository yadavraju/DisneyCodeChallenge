package com.raju.disney.ui.activity

import TOAST
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.raju.disney.R
import com.raju.disney.data.FlightData
import com.raju.disney.opentelemetry.OtelConfiguration
import com.raju.disney.opentelemetry.OtelConfiguration.createChildSpan
import com.raju.disney.opentelemetry.OtelConfiguration.createSpan
import com.raju.disney.opentelemetry.OtelConfiguration.getOpenTelemetry
import com.raju.disney.ui.activity.viewmodel.FlightActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapSetter
import kotlinx.android.synthetic.main.activity_flight.*
import java.net.HttpURLConnection


@AndroidEntryPoint
class FlightActivity : AppCompatActivity() {
    private val otelConfiguration = getOpenTelemetry()
    private val tracer: Tracer = otelConfiguration.getTracer("FlightActivity")
    private val parentSpan = tracer.createSpan("FlightActivity:api:request")
    private val viewModel: FlightActivityViewModel by viewModels()
//    private val textMapPropagator = OtelConfiguration.getTextMapPropagator()
//    private val setter =
//        TextMapSetter { httpURLConnection: HttpURLConnection?, key: String?, value: String? ->
//            httpURLConnection?.setRequestProperty(
//                key,
//                value
//            )
//        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight)
        try {
            parentSpan.makeCurrent().use {
                val propagators: ContextPropagators = otelConfiguration.propagators
                val textMapPropagator = propagators.textMapPropagator

                //textMapPropagator.inject(Context.current(), null, setter)
                Log.e("Raju", "spanContext " + parentSpan.spanContext)
                Log.e("Raju", "traceId: " + parentSpan.spanContext.traceId)
                Log.e("Raju", "spanId: " + parentSpan.spanContext.spanId)
                Log.e("Raju", "traceFlags: " + parentSpan.spanContext.traceFlags)
                Log.e("Raju", "traceFlags: " + parentSpan.spanContext.traceState)

                val map: MutableMap<String, String> = HashMap()
                val setter1 = TextMapSetter<MutableMap<String, String>> { map, key, value ->
                    map?.set(
                        key,
                        value
                    )
                }
                textMapPropagator.inject(Context.current(), map, setter1)
                Log.e("Raju", "map " + map.keys)
                Log.e("Raju", "map " + map.values)
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