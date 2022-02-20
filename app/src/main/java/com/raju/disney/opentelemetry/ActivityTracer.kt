package com.raju.disney.opentelemetry

import android.app.Activity
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import java.util.concurrent.atomic.AtomicReference

internal class ActivityTracer(
    activity: Activity,
    private val initialAppActivity: AtomicReference<String>,
    private val tracer: Tracer,
    visibleScreenTracker: VisibleScreenTracker,
    private val appStartupTimer: AppStartupTimer
) {
    private val activityName: String by lazy { activity.javaClass.simpleName }
    private val activeSpan: ActiveSpan by lazy { ActiveSpan(visibleScreenTracker) }

    fun startSpanIfNoneInProgress(action: String): ActivityTracer {
        if (activeSpan.spanInProgress()) {
            return this
        }
        activeSpan.startSpan { createSpan(action) }
        return this
    }

    fun startActivityCreation(): ActivityTracer {
        activeSpan.startSpan { makeCreationSpan() }
        return this
    }

    private fun makeCreationSpan(): Span {
        //If the application has never loaded an activity, or this is the initial activity getting re-created,
        // we name this span specially to show that it's the application starting up. Otherwise, use
        // the activity class name as the base of the span name.
        val isColdStart = initialAppActivity.get() == null
        if (isColdStart) {
            return createSpanWithParent("Created:$activityName", appStartupTimer.startupSpan)
        }
        return if (activityName == initialAppActivity.get()) {
            createAppStartSpan("warm:$activityName")
        } else createSpan("Created:$activityName")
    }

    fun initiateRestartSpanIfNecessary(multiActivityApp: Boolean): ActivityTracer {
        if (activeSpan.spanInProgress()) {
            return this
        }
        activeSpan.startSpan { makeRestartSpan(multiActivityApp) }
        return this
    }

    private fun makeRestartSpan(multiActivityApp: Boolean): Span {
        //restarting the first activity is a "hot" AppStart
        //Note: in a multi-activity application, navigating back to the first activity can trigger
        //this, so it would not be ideal to call it an AppStart.
        return if (!multiActivityApp && activityName == initialAppActivity.get()) {
            createAppStartSpan("hot:$activityName")
        } else createSpan("Restarted:$activityName")
    }

    private fun createAppStartSpan(startType: String): Span {
        val span = createSpan(APP_START_SPAN_NAME)
        span.setAttribute(DisneyOTel.START_TYPE_KEY, startType)
        //override the component to be appstart
        span.setAttribute(DisneyOTel.COMPONENT_KEY, DisneyOTel.COMPONENT_APP_START)
        return span
    }

    private fun createSpan(spanName: String): Span {
        return createSpanWithParent(spanName, null)
    }

    private fun createSpanWithParent(spanName: String, parentSpan: Span?): Span {
        val spanBuilder = tracer.spanBuilder(spanName)
            .setAttribute(ACTIVITY_NAME_KEY, activityName)
            .setAttribute(DisneyOTel.COMPONENT_KEY, DisneyOTel.COMPONENT_UI)
        if (parentSpan != null) {
            spanBuilder.setParent(parentSpan.storeInContext(Context.current()))
        }
        val span = spanBuilder.startSpan()
        //do this after the span is started, so we can override the default screen.name set by the AttributeAppender.
        span.setAttribute(DisneyOTel.SCREEN_NAME_KEY, activityName)
        return span
    }

    fun endSpanForActivityResumed() {
        if (initialAppActivity.get() == null) {
            initialAppActivity.set(activityName)
        }
        endActiveSpan()
    }

    fun endActiveSpan() {
        // If we happen to be in app startup, make sure this ends it. It's harmless if we're already out of the startup phase.
        appStartupTimer.end()
        activeSpan.endActiveSpan()
    }

    fun addPreviousScreenAttribute(): ActivityTracer {
        activeSpan.addPreviousScreenAttribute(activityName)
        return this
    }

    fun addEvent(eventName: String): ActivityTracer {
        activeSpan.addEvent(eventName)
        return this
    }

    companion object {
        val ACTIVITY_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("activityName")
        const val APP_START_SPAN_NAME = "AppStart"
    }
}