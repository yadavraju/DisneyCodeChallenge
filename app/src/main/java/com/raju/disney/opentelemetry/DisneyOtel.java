package com.raju.disney.opentelemetry;

import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class DisneyOtel {
    private static final AppStartupTimer startupTimer = new AppStartupTimer();

    static final AttributeKey<String> COMPONENT_KEY = AttributeKey.stringKey("component");
    static final AttributeKey<String> SCREEN_NAME_KEY = AttributeKey.stringKey("screen.name");
    static final AttributeKey<String> LAST_SCREEN_NAME_KEY = AttributeKey.stringKey("last.screen.name");
    static final AttributeKey<String> ERROR_TYPE_KEY = stringKey("error.type");
    static final AttributeKey<String> ERROR_MESSAGE_KEY = stringKey("error.message");
    static final AttributeKey<String> WORKFLOW_NAME_KEY = stringKey("workflow.name");
    static final AttributeKey<String> START_TYPE_KEY = stringKey("start.type");

    static final String COMPONENT_APP_START = "appstart";
    static final String COMPONENT_CRASH = "crash";
    static final String COMPONENT_ERROR = "error";
    static final String COMPONENT_UI = "ui";
    static final String LOG_TAG = "DisneyOtel";
    static final String DISNEY_TRACER_NAME = "DisneyOtel";

    private static DisneyOtel INSTANCE;

    private final OpenTelemetrySdk openTelemetrySdk;
    private final Config config;
    private final SessionId sessionId;

    DisneyOtel(OpenTelemetrySdk openTelemetrySdk, SessionId sessionId, Config config) {
        this.openTelemetrySdk = openTelemetrySdk;
        this.config = config;
        this.sessionId = sessionId;
    }

    public static Config.Builder newConfigBuilder() {
        return Config.builder();
    }

    public static DisneyOtel initialize(Config config, Application application) {
        if (INSTANCE != null) {
            Log.w(LOG_TAG, "Singleton Disney Otel instance has already been initialized.");
            return INSTANCE;
        }

        INSTANCE = new OtelInitializer(config, application, startupTimer).initialize(Looper.getMainLooper());

        if (config.isDebugEnabled()) {
            Log.i(LOG_TAG, "Disney Otel monitoring initialized with session ID: " + INSTANCE.sessionId);
        }

        return INSTANCE;
    }

    /**
     * Get the singleton instance of this class.
     */
    public static DisneyOtel getInstance() {
        if (INSTANCE == null) {
            Log.d(LOG_TAG, "Disney Otel not initialized. Returning no-op implementation");
            return NoOpDisneyOtel.INSTANCE;
        }
        return INSTANCE;
    }

    /**
     * Get a handle to the instance of the OpenTelemetry API that this instance is using for instrumentation.
     */
    public OpenTelemetry getOpenTelemetry() {
        return openTelemetrySdk;
    }

    /**
     * Note: this value can change throughout the lifetime of an application instance, so it
     * is recommended that you do not cache this value, but always retrieve it from here when
     * needed.
     */
    public String getOtelSessionId() {
        return sessionId.getSessionId();
    }

    /**
     * Add a custom event to Disney Otel monitoring. This can be useful to capture business events, or
     * simply add instrumentation to your application.
     * <p>
     * This event will be turned into a Span and sent to the disney ingest along with other, auto-generated
     * spans.
     *
     * @param name       The name of the event.
     * @param attributes Any {@link Attributes} to associate with the event.
     */
    public void addEvent(String name, Attributes attributes) {
        getTracer()
                .spanBuilder(name)
                .setAllAttributes(attributes)
                .startSpan()
                .end();
    }

    /**
     * Start a Span to time a named workflow.
     *
     * @param workflowName The name of the workflow to start.
     * @return A {@link Span} that has been started.
     */
    public Span startWorkflow(String workflowName) {
        return getTracer()
                .spanBuilder(workflowName)
                .setAttribute(WORKFLOW_NAME_KEY, workflowName)
                .startSpan();
    }

    /**
     * Start a Span to time a named workflow.
     *
     * @param workflowName The name of the workflow to start.
     * @return A {@link Span} that has been started.
     */
    public Span startChildWorkflow(String workflowName, Span parentSpan) {
        return getTracer()
                .spanBuilder(workflowName)
                .setParent(parentSpan.storeInContext(Context.current()))
                .setAttribute(WORKFLOW_NAME_KEY, workflowName)
                .startSpan();
    }

    public TextMapPropagator getTextMapPropagator() {
        return openTelemetrySdk.getPropagators().getTextMapPropagator();
    }

    public HashMap<String, String> injectSpanContext() {
        HashMap<String, String> map = new HashMap<>();
        TextMapSetter<Map<String, String>> textMapSetter = (carrier, key, value) -> carrier.put(key, value);
        getTextMapPropagator().inject(Context.current(), map, textMapSetter);
        return map;
    }


    /**
     * Add a custom exception to Disney otel monitoring. This can be useful for tracking custom error
     * handling in your application.
     * <p>
     * This event will be turned into a Span and sent to the Disney ingest along with other, auto-generated
     * spans.
     *
     * @param throwable A {@link Throwable} associated with this event.
     */
    public void addDisneyOtelException(Throwable throwable) {
        addDisneyOtelException(throwable, Attributes.empty());
    }

    /**
     * Add a custom exception to Disney otel monitoring. This can be useful for tracking custom error
     * handling in your application.
     * <p>
     * This event will be turned into a Span and sent to the Disney ingest along with other, auto-generated
     * spans.
     *
     * @param throwable  A {@link Throwable} associated with this event.
     * @param attributes Any {@link Attributes} to associate with the event.
     */
    public void addDisneyOtelException(Throwable throwable, Attributes attributes) {
        Span span = getTracer()
                .spanBuilder(throwable.getClass().getSimpleName())
                .setAllAttributes(attributes)
                .setAttribute(COMPONENT_KEY, COMPONENT_ERROR)
                .startSpan();
        addExceptionAttributes(span, throwable);
        span.end();
    }

    Tracer getTracer() {
        return openTelemetrySdk.getTracer(DISNEY_TRACER_NAME);
    }

    static void addExceptionAttributes(Span span, Throwable e) {
        addExceptionAttributes((key, value) -> span.setAttribute((AttributeKey<? super Object>) key, value), e);
    }

    static void addExceptionAttributes(BiConsumer<AttributeKey<?>, Object> setAttribute, Throwable e) {
        setAttribute.accept(SemanticAttributes.EXCEPTION_TYPE, e.getClass().getSimpleName());
        setAttribute.accept(SemanticAttributes.EXCEPTION_MESSAGE, e.getMessage());

        setAttribute.accept(ERROR_TYPE_KEY, e.getClass().getSimpleName());
        setAttribute.accept(ERROR_MESSAGE_KEY, e.getMessage());
    }

    void recordAnr(StackTraceElement[] stackTrace) {
        getTracer()
                .spanBuilder("ANR")
                .setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, formatStackTrace(stackTrace))
                .setAttribute(COMPONENT_KEY, COMPONENT_ERROR)
                .startSpan()
                .setStatus(StatusCode.ERROR)
                .end();
    }

    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuilder.append(stackTraceElement).append("\n");
        }
        return stringBuilder.toString();
    }
}
