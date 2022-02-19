package com.raju.disney.opentelemetry;

import androidx.fragment.app.Fragment;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

class FragmentTracer {
    static final AttributeKey<String> FRAGMENT_NAME_KEY = AttributeKey.stringKey("fragmentName");

    private final String fragmentName;
    private final Tracer tracer;
    private final ActiveSpan activeSpan;

    FragmentTracer(Fragment fragment, Tracer tracer, VisibleScreenTracker visibleScreenTracker) {
        this.tracer = tracer;
        this.fragmentName = fragment.getClass().getSimpleName();
        this.activeSpan = new ActiveSpan(visibleScreenTracker);
    }

    FragmentTracer startSpanIfNoneInProgress(String action) {
        if (activeSpan.spanInProgress()) {
            return this;
        }
        activeSpan.startSpan(() -> createSpan(action));
        return this;
    }

    FragmentTracer startFragmentCreation() {
        activeSpan.startSpan(() -> createSpan("Created"));
        return this;
    }

    private Span createSpan(String spanName) {
        Span span = tracer.spanBuilder(spanName)
                .setAttribute(FRAGMENT_NAME_KEY, fragmentName)
                .setAttribute(DisneyOtel.COMPONENT_KEY, DisneyOtel.COMPONENT_UI).startSpan();
        span.setAttribute(DisneyOtel.SCREEN_NAME_KEY, fragmentName);
        return span;
    }

    void endActiveSpan() {
        activeSpan.endActiveSpan();
    }

    FragmentTracer addPreviousScreenAttribute() {
        activeSpan.addPreviousScreenAttribute(fragmentName);
        return this;
    }

    FragmentTracer addEvent(String eventName) {
        activeSpan.addEvent(eventName);
        return this;
    }
}
