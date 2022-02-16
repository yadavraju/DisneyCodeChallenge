package com.raju.disney.ui.activity

import android.os.Bundle
import com.raju.disney.R
import com.raju.disney.base.BaseActivity
import com.raju.disney.opentelemetry.OtelConfiguration
import com.raju.disney.opentelemetry.OtelConfiguration.createSpan
import com.raju.disney.ui.fragment.MovieDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import io.opentelemetry.api.trace.Tracer

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val tracer: Tracer = OtelConfiguration.getTracer("app:MainActivity")
    private val span = tracer.createSpan("MainActivity:OpenFragment")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            span.makeCurrent().use {
                if (savedInstanceState == null) {
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.fragmentContainer, MovieDetailFragment.newInstance())
                        .commit()
                }
            }
        } finally {
            span.end()
        }
    }
}
