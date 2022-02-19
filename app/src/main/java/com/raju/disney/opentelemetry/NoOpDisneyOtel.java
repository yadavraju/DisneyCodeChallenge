package com.raju.disney.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

class NoOpDisneyOtel extends DisneyOtel {
    static final NoOpDisneyOtel INSTANCE = new NoOpDisneyOtel();

    private NoOpDisneyOtel() {
        super(null, null, null);
    }


    @Override
    public OpenTelemetry getOpenTelemetry() {
        return OpenTelemetry.noop();
    }

    @Override
    Tracer getTracer() {
        return getOpenTelemetry().getTracer("unused");
    }
}
