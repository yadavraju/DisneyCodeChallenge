package com.raju.disney.opentelemetry

import com.raju.disney.opentelemetry.DisneyOTel.Companion.addExceptionAttributes
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import java.io.PrintWriter
import java.io.StringWriter

internal object CrashReporter {

    fun initializeCrashReporting(tracer: Tracer, openTelemetrySdk: OpenTelemetrySdk) {
        val existingHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            CrashReportingExceptionHandler(
                tracer,
                openTelemetrySdk.sdkTracerProvider,
                existingHandler
            )
        )
    }

    internal class CrashReportingExceptionHandler(
        private val tracer: Tracer,
        private val sdkTracerProvider: SdkTracerProvider,
        private val existingHandler: Thread.UncaughtExceptionHandler?
    ) : Thread.UncaughtExceptionHandler {

        override fun uncaughtException(t: Thread, e: Throwable) {
            val writer = StringWriter()
            e.printStackTrace(PrintWriter(writer))
            val exceptionType = e.javaClass.simpleName
            val span = tracer.spanBuilder(exceptionType)
                .setAttribute(SemanticAttributes.THREAD_ID, t.id)
                .setAttribute(SemanticAttributes.THREAD_NAME, t.name)
                .setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString())
                .setAttribute(SemanticAttributes.EXCEPTION_ESCAPED, true)
                .setAttribute(DisneyOTel.COMPONENT_KEY, DisneyOTel.COMPONENT_CRASH)
                .startSpan()
            addExceptionAttributes(span, e)
            span.setStatus(StatusCode.ERROR).end()
            //do our best to make sure the crash makes it out of the VM
            sdkTracerProvider.forceFlush()
            //preserve any existing behavior:
            existingHandler?.uncaughtException(t, e)
        }
    }
}