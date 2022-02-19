package com.raju.disney.opentelemetry;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.DEVICE_MODEL_IDENTIFIER;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.DEVICE_MODEL_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.OS_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.OS_TYPE;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.OS_VERSION;

import android.os.Build;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

class AttributeAppender implements SpanProcessor {
    static final AttributeKey<String> APP_NAME_KEY = stringKey("app");
    static final AttributeKey<String> SESSION_ID_KEY = stringKey("disney.otelSessionId");

    private final Config config;
    private final SessionId sessionId;
    private final VisibleScreenTracker visibleScreenTracker;

    AttributeAppender(Config config, SessionId sessionId, VisibleScreenTracker visibleScreenTracker) {
        this.config = config;
        this.sessionId = sessionId;
        this.visibleScreenTracker = visibleScreenTracker;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        span.setAttribute(APP_NAME_KEY, config.getApplicationName());
        span.setAttribute(SESSION_ID_KEY, sessionId.getSessionId());

        span.setAttribute(DEVICE_MODEL_NAME, Build.MODEL);
        span.setAttribute(DEVICE_MODEL_IDENTIFIER, Build.MODEL);
        span.setAttribute(OS_NAME, "Android");
        span.setAttribute(OS_TYPE, "linux");
        span.setAttribute(OS_VERSION, Build.VERSION.RELEASE);

        String currentScreen = visibleScreenTracker.getCurrentlyVisibleScreen();
        span.setAttribute(DisneyOtel.SCREEN_NAME_KEY, currentScreen);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
