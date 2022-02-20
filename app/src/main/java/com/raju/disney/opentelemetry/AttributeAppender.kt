package com.raju.disney.opentelemetry

import android.os.Build
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes

internal class AttributeAppender(
    private val config: Config,
    private val sessionId: SessionId,
    private val visibleScreenTracker: VisibleScreenTracker
) : SpanProcessor {

    override fun onStart(parentContext: Context, span: ReadWriteSpan) {
        span.setAttribute(APP_NAME_KEY, config.applicationName)
        span.setAttribute(SESSION_ID_KEY, sessionId.sessionId)
        span.setAttribute(ResourceAttributes.DEVICE_MODEL_NAME, Build.MODEL)
        span.setAttribute(ResourceAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
        span.setAttribute(ResourceAttributes.OS_NAME, "Android")
        span.setAttribute(ResourceAttributes.OS_TYPE, "linux")
        span.setAttribute(ResourceAttributes.OS_VERSION, Build.VERSION.RELEASE)
        val currentScreen = visibleScreenTracker.currentlyVisibleScreen
        span.setAttribute(DisneyOTel.SCREEN_NAME_KEY, currentScreen)
    }

    override fun isStartRequired(): Boolean {
        return true
    }

    override fun onEnd(span: ReadableSpan) {}

    override fun isEndRequired(): Boolean {
        return false
    }

    companion object {
        val APP_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("app")
        val SESSION_ID_KEY: AttributeKey<String> = AttributeKey.stringKey("disney.otelSessionId")
    }
}