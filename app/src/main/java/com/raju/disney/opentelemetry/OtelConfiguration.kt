/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.raju.disney.opentelemetry

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import java.util.concurrent.TimeUnit


/**
 * All SDK management takes place here, away from the instrumentation code, which should only access
 * the OpenTelemetry APIs.
 */
object OtelConfiguration {

    private fun init(): OpenTelemetry {
        // Export traces to Jaeger todo remove this ThreadPolicy code
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val jaegerExporter = JaegerGrpcSpanExporter.builder()
            .setEndpoint("http://192.168.0.102:14250")
            .setTimeout(30, TimeUnit.SECONDS)
            .build()
//        val endpoint = String.format("http://%s:%s/api/v2/spans", "192.168.0.102", "9411")
//        val zipkinExporter = ZipkinSpanExporter.builder().setEndpoint(endpoint).build()
//        val oltpExporter =
//            OtlpGrpcSpanExporter.builder().setEndpoint("http://192.168.0.102:4317")
//                .setTimeout(2, TimeUnit.SECONDS).build()
        val serviceNameResource =
            Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "DisneyAndroid"))

        // Set to process the spans by the Jaeger Exporter
        val tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
//            .addSpanProcessor(SimpleSpanProcessor.create(oltpExporter))
//            .addSpanProcessor(SimpleSpanProcessor.create(zipkinExporter))
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .setResource(Resource.getDefault().merge(serviceNameResource))
            .build()

        val openTelemetry = OpenTelemetrySdk
            .builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()

        // it's always a good idea to shut down the SDK cleanly at JVM exit.
        Runtime.getRuntime().addShutdownHook(Thread { tracerProvider.close() })
        return openTelemetry
    }

    fun getOpenTelemetry(): OpenTelemetry {
        return init()
    }

    fun getTracer(tracerName: String): Tracer {
        return init().getTracer(tracerName)
    }

    fun Tracer.createSpan(parentSpanName: String): Span =
        this.spanBuilder(parentSpanName).setSpanKind(SpanKind.CLIENT).startSpan()


    fun Tracer.createChildSpan(childSpanName: String, parentSpan: Span?): Span =
        this.spanBuilder(childSpanName)
            .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
            .setSpanKind(SpanKind.CLIENT).startSpan()

    fun getTextMapPropagator(): TextMapPropagator = init().propagators.textMapPropagator

}