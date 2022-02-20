package com.raju.disney.opentelemetry

import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Scope
import java.util.function.Supplier

internal class ActiveSpan(private val visibleScreenTracker: VisibleScreenTracker) {

    private var span: Span? = null
    private var scope: Scope? = null

    fun spanInProgress(): Boolean {
        return span != null
    }

    fun startSpan(spanCreator: Supplier<Span?>) {
        //don't start one if there's already one in progress
        if (span != null) {
            return
        }
        span = spanCreator.get()
        scope = span?.makeCurrent()
    }

    fun endActiveSpan() {
        if (scope != null) {
            scope?.close()
            scope = null
        }
        if (span != null) {
            span?.end()
            span = null
        }
    }

    fun addEvent(eventName: String) {
        span?.addEvent(eventName)
    }

    fun addPreviousScreenAttribute(screenName: String) {
        if (span == null) {
            return
        }
        val previouslyVisibleScreen = visibleScreenTracker.previouslyVisibleScreen
        if (screenName != previouslyVisibleScreen) {
            span?.setAttribute(DisneyOTel.LAST_SCREEN_NAME_KEY, previouslyVisibleScreen)
        }
    }
}