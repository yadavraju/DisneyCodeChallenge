package com.raju.disney.opentelemetry;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class OtelInitializer {

    private final Config config;
    private final Application application;
    private final AppStartupTimer startupTimer;
    private final AnchoredClock timingClock;
    private final List<OtelInitializer.InitializationEvent> initializationEvents = new ArrayList<>();

    public OtelInitializer(Config config, Application application, AppStartupTimer startupTimer) {
        this.config = config;
        this.application = application;
        this.startupTimer = startupTimer;
        this.timingClock = startupTimer.startupClock;
    }

    public DisneyOtel initialize(Looper mainLooper) {
        VisibleScreenTracker visibleScreenTracker = new VisibleScreenTracker();
        long startTimeNanos = timingClock.now();

        OtlpGrpcSpanExporter oltpExporter =
                OtlpGrpcSpanExporter.builder().setEndpoint(config.getOltpExporterEndPoint())
                        .setTimeout(2, TimeUnit.SECONDS).build();
        initializationEvents.add(new OtelInitializer.InitializationEvent("OtlpGrpcSpanExporterInitialized", timingClock.now()));

        SessionId sessionId = new SessionId();
        initializationEvents.add(new OtelInitializer.InitializationEvent("sessionIdInitialized", timingClock.now()));

        SdkTracerProvider tracerProvider = buildTracerProvider(Clock.getDefault(), oltpExporter, sessionId, visibleScreenTracker);
        initializationEvents.add(new OtelInitializer.InitializationEvent("tracerProviderInitialized", timingClock.now()));

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk
                .builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        initializationEvents.add(new OtelInitializer.InitializationEvent("openTelemetrySdkInitialized", timingClock.now()));

        List<AppStateListener> appStateListeners = new ArrayList<>();
        if (config.isAnrDetectionEnabled()) {
            appStateListeners.add(initializeAnrReporting(mainLooper));
            initializationEvents.add(new OtelInitializer.InitializationEvent("anrMonitorInitialized", timingClock.now()));
        }
        Tracer tracer = openTelemetry.getTracer(DisneyOtel.DISNEY_TRACER_NAME);
        sessionId.setSessionIdChangeListener(new SessionIdChangeTracer(tracer));

        if (Build.VERSION.SDK_INT < 29) {
            application.registerActivityLifecycleCallbacks(new Pre29ActivityCallbacks(tracer, visibleScreenTracker, startupTimer, appStateListeners));
        } else {
            ActivityCallbacks activityCallbacks = ActivityCallbacks.builder()
                    .tracer(tracer)
                    .visibleScreenTracker(visibleScreenTracker)
                    .startupTimer(startupTimer)
                    .appStateListeners(appStateListeners)
                    .build();
            application.registerActivityLifecycleCallbacks(activityCallbacks);
        }
        initializationEvents.add(new OtelInitializer.InitializationEvent("activityLifecycleCallbacksInitialized", timingClock.now()));

        CrashReporter.initializeCrashReporting(tracer, openTelemetry);
        initializationEvents.add(new OtelInitializer.InitializationEvent("crashReportingInitialized", timingClock.now()));

        recordInitializationSpans(startTimeNanos, initializationEvents, tracer, config);
        return new DisneyOtel(openTelemetry, sessionId, config);
    }

    private SdkTracerProvider buildTracerProvider(
            Clock clock,
            SpanExporter exporter,
            SessionId sessionId,
            VisibleScreenTracker visibleScreenTracker) {
        BatchSpanProcessor batchSpanProcessor = BatchSpanProcessor.builder(exporter).build();
        initializationEvents.add(new OtelInitializer.InitializationEvent("batchSpanProcessorInitialized", timingClock.now()));

        AttributeAppender attributeAppender = new AttributeAppender(config, sessionId, visibleScreenTracker);
        initializationEvents.add(new OtelInitializer.InitializationEvent("attributeAppenderInitialized", timingClock.now()));

        Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, config.getApplicationName()).build();
        initializationEvents.add(new OtelInitializer.InitializationEvent("resourceInitialized", timingClock.now()));

        SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder()
                .setClock(clock)
                .addSpanProcessor(batchSpanProcessor)
                .addSpanProcessor(attributeAppender)
                .setSpanLimits(SpanLimits.builder().setMaxAttributeValueLength(2048).build())
                .setResource(resource);
        initializationEvents.add(new OtelInitializer.InitializationEvent("tracerProviderBuilderInitialized", timingClock.now()));

        if (config.isDebugEnabled()) {
            //Todo need to remove this StrictMode policy for testing only we are using jaegerExporter
            // latter we will remove this that time we don't need Stric mode policy
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                    .setEndpoint(config.getJaegerExporterEndPoint())
                    .setTimeout(30, TimeUnit.SECONDS)
                    .build();
            tracerProviderBuilder
                    .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                    .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter));
            initializationEvents.add(new OtelInitializer.InitializationEvent("debugSpanExporterInitialized", timingClock.now()));
        }
        return tracerProviderBuilder.build();
    }


    private void recordInitializationSpans(long startTimeNanos, List<InitializationEvent> initializationEvents, Tracer tracer, Config config) {
        Span overallAppStart = startupTimer.start(tracer);
        Span span = tracer.spanBuilder("DisneyOtel.initialize")
                .setParent(Context.current().with(overallAppStart))
                .setStartTimestamp(startTimeNanos, TimeUnit.NANOSECONDS)
                .setAttribute(DisneyOtel.COMPONENT_KEY, DisneyOtel.COMPONENT_APP_START)
                .startSpan();

        String configSettings = "[debug:" + config.isDebugEnabled() + "," +
                "crashReporting:" + true + "," +
                "anrReporting:" + config.isAnrDetectionEnabled() + "]";
        span.setAttribute("config_settings", configSettings);

        for (OtelInitializer.InitializationEvent initializationEvent : initializationEvents) {
            span.addEvent(initializationEvent.name, initializationEvent.time, TimeUnit.NANOSECONDS);
        }
        span.end(timingClock.now(), TimeUnit.NANOSECONDS);
    }

    private AppStateListener initializeAnrReporting(Looper mainLooper) {
        Thread mainThread = mainLooper.getThread();
        Handler uiHandler = new Handler(mainLooper);
        AnrWatcher anrWatcher = new AnrWatcher(uiHandler, mainThread, DisneyOtel::getInstance);
        ScheduledExecutorService anrScheduler = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> scheduledFuture = anrScheduler.scheduleAtFixedRate(anrWatcher, 1, 1, TimeUnit.SECONDS);
        return new AppStateListener() {
            private ScheduledFuture<?> future = scheduledFuture;

            @Override
            public void appForegrounded() {
                if (future == null) {
                    future = anrScheduler.scheduleAtFixedRate(anrWatcher, 1, 1, TimeUnit.SECONDS);
                }
            }

            @Override
            public void appBackgrounded() {
                if (future != null) {
                    future.cancel(true);
                    future = null;
                }
            }
        };
    }

    static class InitializationEvent {
        private final String name;
        private final long time;

        private InitializationEvent(String name, long time) {
            this.name = name;
            this.time = time;
        }
    }

    //copied from otel-java
    static final class AnchoredClock {
        private final Clock clock;
        private final long epochNanos;
        private final long nanoTime;

        private AnchoredClock(Clock clock, long epochNanos, long nanoTime) {
            this.clock = clock;
            this.epochNanos = epochNanos;
            this.nanoTime = nanoTime;
        }

        public static AnchoredClock create(Clock clock) {
            return new AnchoredClock(clock, clock.now(), clock.nanoTime());
        }

        long now() {
            long deltaNanos = this.clock.nanoTime() - this.nanoTime;
            return this.epochNanos + deltaNanos;
        }
    }

}
