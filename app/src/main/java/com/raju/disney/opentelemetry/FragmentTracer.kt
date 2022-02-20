package com.raju.disney.opentelemetry

import androidx.fragment.app.Fragment
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer

internal class FragmentTracer(
    fragment: Fragment,
    private val tracer: Tracer,
    visibleScreenTracker: VisibleScreenTracker
) {

    private val fragmentName: String by lazy { fragment.javaClass.simpleName }
    private val activeSpan: ActiveSpan by lazy { ActiveSpan(visibleScreenTracker) }

    fun startSpanIfNoneInProgress(action: String): FragmentTracer {
        if (activeSpan.spanInProgress()) {
            return this
        }
        activeSpan.startSpan { createSpan(action) }
        return this
    }

    fun startFragmentCreation(): FragmentTracer {
        activeSpan.startSpan { createSpan("Created: $fragmentName") }
        return this
    }

    private fun createSpan(spanName: String): Span {
        val span = tracer.spanBuilder(spanName)
            .setAttribute(FRAGMENT_NAME_KEY, fragmentName)
            .setAttribute(DisneyOTel.COMPONENT_KEY, DisneyOTel.COMPONENT_UI).startSpan()
        span.setAttribute(DisneyOTel.SCREEN_NAME_KEY, fragmentName)
        return span
    }

    fun endActiveSpan() {
        activeSpan.endActiveSpan()
    }

    fun addPreviousScreenAttribute(): FragmentTracer {
        activeSpan.addPreviousScreenAttribute(fragmentName)
        return this
    }

    fun addEvent(eventName: String): FragmentTracer {
        activeSpan.addEvent(eventName)
        return this
    }

    companion object {
        val FRAGMENT_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("fragmentName")
    }

}