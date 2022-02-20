package com.raju.disney.opentelemetry;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

class CrashReporter {

    static void initializeCrashReporting(Tracer tracer, OpenTelemetrySdk openTelemetrySdk) {
        Thread.UncaughtExceptionHandler existingHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new CrashReportingExceptionHandler(tracer, openTelemetrySdk.getSdkTracerProvider(), existingHandler));
    }

    static class CrashReportingExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Tracer tracer;
        private final Thread.UncaughtExceptionHandler existingHandler;
        private final SdkTracerProvider sdkTracerProvider;

        CrashReportingExceptionHandler(Tracer tracer, SdkTracerProvider sdkTracerProvider, Thread.UncaughtExceptionHandler existingHandler) {
            this.tracer = tracer;
            this.existingHandler = existingHandler;
            this.sdkTracerProvider = sdkTracerProvider;
        }

        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String exceptionType = e.getClass().getSimpleName();
            Span span = tracer.spanBuilder(exceptionType)
                    .setAttribute(SemanticAttributes.THREAD_ID, t.getId())
                    .setAttribute(SemanticAttributes.THREAD_NAME, t.getName())
                    .setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString())
                    .setAttribute(SemanticAttributes.EXCEPTION_ESCAPED, true)
                    .setAttribute(DisneyOtel.COMPONENT_KEY, DisneyOtel.COMPONENT_CRASH)
                    .startSpan();
            DisneyOtel.addExceptionAttributes(span, e);
            span.setStatus(StatusCode.ERROR).end();
            //do our best to make sure the crash makes it out of the VM
            sdkTracerProvider.forceFlush();
            //preserve any existing behavior:
            if (existingHandler != null) {
                existingHandler.uncaughtException(t, e);
            }
        }
    }
}