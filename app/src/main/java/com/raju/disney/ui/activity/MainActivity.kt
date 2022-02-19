package com.raju.disney.ui.activity

import android.os.Bundle
import android.util.Log
import com.raju.disney.R
import com.raju.disney.base.BaseActivity
import com.raju.disney.ui.fragment.MovieDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer

@AndroidEntryPoint
class MainActivity : BaseActivity() {

//    private val tracer: Tracer =
//        DisneyOtel.getInstance().openTelemetry.getTracer("MainActivity")//OtelConfiguration.getTracer()
//    private val span = tracer.createSpan("MainActivity:OpenFragment")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val otel: DisneyOtel = DisneyOtel.getInstance()
//        val span: Span = otel.startWorkflow("MainActivity:OpenFragment")
//
//        Log.e("Raju", "TraceId:Main " + span.spanContext.traceId)
//        Log.e("Raju", "spanId:Main " + span.spanContext.spanId)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, MovieDetailFragment.newInstance())
                .commit()
        }

//        span.end()
//        try {
//            span.makeCurrent().use {
//
//            }
//        } finally {
//            span.end()
//        }
    }
}
