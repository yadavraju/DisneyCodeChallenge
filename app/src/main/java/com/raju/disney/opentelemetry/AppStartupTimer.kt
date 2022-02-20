package com.raju.disney.opentelemetry

import com.raju.disney.opentelemetry.OTelInitializer.AnchoredClock.Companion.create
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.common.Clock
import java.util.concurrent.TimeUnit

class AppStartupTimer {
    //exposed so it can be used for the rest of the startup sequence timing.
    val startupClock by lazy { create(Clock.getDefault()) }
    private val firstPossibleTimestamp by lazy { startupClock.now() }

    @Volatile
    var startupSpan: Span? = null
        private set

    fun start(tracer: Tracer): Span {
        //guard against a double-start and just return what's already in flight.
        if (startupSpan != null) {
            return startupSpan!!
        }
        val appStart = tracer.spanBuilder("AppStart")
            .setStartTimestamp(firstPossibleTimestamp, TimeUnit.NANOSECONDS)
            .setAttribute(DisneyOTel.COMPONENT_KEY, DisneyOTel.COMPONENT_APP_START)
            .setAttribute(DisneyOTel.START_TYPE_KEY, "cold")
            .startSpan()
        startupSpan = appStart
        return appStart
    }

    fun end() {
        if (startupSpan != null) {
            startupSpan?.end(startupClock.now(), TimeUnit.NANOSECONDS)
            startupSpan = null
        }
    }
}