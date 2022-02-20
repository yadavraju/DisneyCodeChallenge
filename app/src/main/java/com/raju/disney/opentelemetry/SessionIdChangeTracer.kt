package com.raju.disney.opentelemetry

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Tracer

internal class SessionIdChangeTracer(private val tracer: Tracer) : SessionIdChangeListener {

    override fun onChange(oldSessionId: String, newSessionId: String) {
        tracer.spanBuilder("sessionId.change")
            .setAttribute(PREVIOUS_SESSION_ID_KEY, oldSessionId)
            .startSpan()
            .end()
    }

    companion object {
        val PREVIOUS_SESSION_ID_KEY: AttributeKey<String> =
            AttributeKey.stringKey("disney.oTel.previous_session_id")
    }
}