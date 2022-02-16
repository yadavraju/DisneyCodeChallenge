package com.raju.disney.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.extension.annotations.WithSpan;

public final class JaegerExample {

    private final Tracer tracer;

    public JaegerExample(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer("io.opentelemetry.example.JaegerExample");
    }

    public void myWonderfulUseCase() {
        // Generate a span
        Span span = this.tracer.spanBuilder("Start my wonderful use case").setSpanKind(SpanKind.CLIENT).startSpan();
        span.addEvent("Event 0");
        // execute my use case - here we simulate a wait
        try (Scope scope = span.makeCurrent()) {
            span.addEvent("Starting the work.");
            sayHello("Raju Android");
            span.addEvent("Finished working.");
        } finally {
            span.addEvent("Event 1");
            span.end();
        }

    }

    private void doWork() {
        try {
            Thread.sleep(1000);
            // sayHello("Raju");
        } catch (InterruptedException e) {
            // do the right thing here
        }
    }

    @WithSpan(kind = SpanKind.CLIENT)
    private void sayHello(String helloTo) {
        Span span = tracer.spanBuilder("say-hello").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("hello-to", helloTo);
            String helloStr = formatString(helloTo);
            printHello(helloStr);
        } catch (Throwable throwable) {
            span.setStatus(StatusCode.ERROR, "Something bad happened!");
            span.recordException(throwable);
        } finally {
            span.end();
        }
    }

    @WithSpan()
    private String formatString(String helloTo) {
        Span span = tracer.spanBuilder("formatString").startSpan();
        try (Scope scope = span.makeCurrent()) {
            String helloStr = String.format("Hello, %s!", helloTo);
            Attributes of = Attributes.of(AttributeKey.stringKey("event"), "string-format",
                    AttributeKey.stringKey("value"), helloStr);
            span.setAllAttributes(of);
            return helloStr;
        } finally {
            span.end();
        }
    }

    @WithSpan()
    private void printHello(String helloStr) {
        Span span = tracer.spanBuilder("printHello").startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println(helloStr);
            span.setAttribute("event", "println");
        } finally {
            span.end();
        }
    }
}
