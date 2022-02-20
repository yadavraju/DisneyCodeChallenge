package com.raju.disney.opentelemetry;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;

class AppStartupTimer {
    //exposed so it can be used for the rest of the startup sequence timing.
    final OtelInitializer.AnchoredClock startupClock = OtelInitializer.AnchoredClock.create(Clock.getDefault());
    private final long firstPossibleTimestamp = startupClock.now();
    private volatile Span overallAppStartSpan = null;

    Span start(Tracer tracer) {
        //guard against a double-start and just return what's already in flight.
        if (overallAppStartSpan != null) {
            return overallAppStartSpan;
        }
        final Span appStart = tracer.spanBuilder("AppStart")
                .setStartTimestamp(firstPossibleTimestamp, TimeUnit.NANOSECONDS)
                .setAttribute(DisneyOtel.COMPONENT_KEY, DisneyOtel.COMPONENT_APP_START)
                .setAttribute(DisneyOtel.START_TYPE_KEY, "cold")
                .startSpan();
        overallAppStartSpan = appStart;
        return appStart;
    }

    void end() {
        if (overallAppStartSpan != null) {
            overallAppStartSpan.end(startupClock.now(), TimeUnit.NANOSECONDS);
            overallAppStartSpan = null;
        }
    }

    @Nullable
    Span getStartupSpan() {
        return overallAppStartSpan;
    }
}
