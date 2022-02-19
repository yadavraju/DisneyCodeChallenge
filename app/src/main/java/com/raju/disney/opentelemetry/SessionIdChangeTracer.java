package com.raju.disney.opentelemetry;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;

final class SessionIdChangeTracer implements SessionIdChangeListener {

    static final AttributeKey<String> PREVIOUS_SESSION_ID_KEY = stringKey("disney.otel.previous_session_id");

    private final Tracer tracer;

    SessionIdChangeTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void onChange(String oldSessionId, String newSessionId) {
        tracer.spanBuilder("sessionId.change")
                .setAttribute(PREVIOUS_SESSION_ID_KEY, oldSessionId)
                .startSpan()
                .end();
    }
}
