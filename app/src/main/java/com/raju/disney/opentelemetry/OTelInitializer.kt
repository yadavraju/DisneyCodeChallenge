package com.raju.disney.opentelemetry

import android.app.Application
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanLimits
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OTelInitializer(
    private val config: Config,
    private val application: Application,
    private val startupTimer: AppStartupTimer
) {
    private val timingClock: AnchoredClock by lazy { startupTimer.startupClock }
    private val initializationEvents: MutableList<InitializationEvent> by lazy { ArrayList() }

    fun initialize(mainLooper: Looper): DisneyOTel {
        val visibleScreenTracker = VisibleScreenTracker()
        val startTimeNanos = timingClock.now()

        val oltpExporter = OtlpGrpcSpanExporter
            .builder()
            .setEndpoint(config.oltpExporterEndPoint)
            .setTimeout(2, TimeUnit.SECONDS)
            .build()

        val sessionId = SessionId()
        val tracerProvider =
            buildTracerProvider(Clock.getDefault(), oltpExporter, sessionId, visibleScreenTracker)

        val openTelemetry = OpenTelemetrySdk
            .builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
        addInitializationEvents("openTelemetrySdkInitialized")

        val appStateListeners: MutableList<AppStateListener> = ArrayList()
        if (config.isAnrDetectionEnabled) {
            appStateListeners.add(initializeAnrReporting(mainLooper))
            addInitializationEvents("anrMonitorInitialized")
        }

        val tracer = openTelemetry.getTracer(DisneyOTel.DISNEY_TRACER_NAME)
        sessionId.setSessionIdChangeListener(SessionIdChangeTracer(tracer))
        if (Build.VERSION.SDK_INT < 29) {
            application.registerActivityLifecycleCallbacks(
                Pre29ActivityCallbacks(
                    tracer,
                    visibleScreenTracker,
                    startupTimer,
                    appStateListeners
                )
            )
        } else {
            val activityCallbacks = ActivityCallbacks.builder()
                .tracer(tracer)
                .visibleScreenTracker(visibleScreenTracker)
                .startupTimer(startupTimer)
                .appStateListeners(appStateListeners)
                .build()
            application.registerActivityLifecycleCallbacks(activityCallbacks)
        }
        addInitializationEvents("activityLifecycleCallbacksInitialized")

        CrashReporter.initializeCrashReporting(tracer, openTelemetry)
        addInitializationEvents("crashReportingInitialized")

        recordInitializationSpans(startTimeNanos, initializationEvents, tracer, config)
        return DisneyOTel(openTelemetry, sessionId, config)
    }

    private fun addInitializationEvents(eventName: String) {
        initializationEvents.add(InitializationEvent(eventName, timingClock.now()))
    }

    private fun buildTracerProvider(
        clock: Clock,
        exporter: SpanExporter,
        sessionId: SessionId,
        visibleScreenTracker: VisibleScreenTracker
    ): SdkTracerProvider {

        val batchSpanProcessor = BatchSpanProcessor.builder(exporter).build()
        val attributeAppender = AttributeAppender(config, sessionId, visibleScreenTracker)

        val resource = Resource.getDefault()
            .toBuilder()
            .put(ResourceAttributes.SERVICE_NAME, config.applicationName)
            .build()

        val tracerProviderBuilder = SdkTracerProvider.builder()
            .setClock(clock)
            .addSpanProcessor(batchSpanProcessor)
            .addSpanProcessor(attributeAppender)
            .setSpanLimits(SpanLimits.builder().setMaxAttributeValueLength(2048).build())
            .setResource(resource)

        addInitializationEvents("tracerProviderBuilderInitialized")

        if (config.isDebugEnabled) {
            //Todo need to remove this StrictMode policy for testing only we are using jaegerExporter
            // latter we will remove this that time we don't need Stric mode policy
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val jaegerExporter = JaegerGrpcSpanExporter.builder()
                .setEndpoint(config.jaegerExporterEndPoint)
                .setTimeout(30, TimeUnit.SECONDS)
                .build()

            tracerProviderBuilder
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))

            addInitializationEvents("debugSpanExporterInitialized")
        }
        return tracerProviderBuilder.build()
    }

    private fun recordInitializationSpans(
        startTimeNanos: Long,
        initializationEvents: List<InitializationEvent>,
        tracer: Tracer,
        config: Config
    ) {
        val overallAppStart = startupTimer.start(tracer)

        val span = tracer.spanBuilder("DisneyOTel.initialize")
            .setParent(Context.current().with(overallAppStart))
            .setStartTimestamp(startTimeNanos, TimeUnit.NANOSECONDS)
            .setAttribute(DisneyOTel.COMPONENT_KEY, DisneyOTel.COMPONENT_APP_START)
            .startSpan()

        val configSettings = "[debug:" + config.isDebugEnabled + "," +
                "crashReporting:" + true + "," +
                "anrReporting:" + config.isAnrDetectionEnabled + "]"
        span.setAttribute("config_settings", configSettings)

        for (initializationEvent in initializationEvents) {
            span.addEvent(initializationEvent.name, initializationEvent.time, TimeUnit.NANOSECONDS)
        }

        span.end(timingClock.now(), TimeUnit.NANOSECONDS)
    }

    private fun initializeAnrReporting(mainLooper: Looper): AppStateListener {
        val mainThread = mainLooper.thread
        val uiHandler = Handler(mainLooper)
        val anrWatcher = AnrWatcher(uiHandler, mainThread, DisneyOTel::instance)
        val anrScheduler = Executors.newScheduledThreadPool(1)
        val scheduledFuture = anrScheduler.scheduleAtFixedRate(anrWatcher, 1, 1, TimeUnit.SECONDS)
        return object : AppStateListener {
            private var future = scheduledFuture
            override fun appForegrounded() {
                if (future == null) {
                    future = anrScheduler.scheduleAtFixedRate(anrWatcher, 1, 1, TimeUnit.SECONDS)
                }
            }

            override fun appBackgrounded() {
                if (future != null) {
                    future.cancel(true)
                    future = null
                }
            }
        }
    }

    internal class InitializationEvent(val name: String, val time: Long)

    //copied from otel-java
    class AnchoredClock private constructor(
        private val clock: Clock,
        private val epochNanos: Long,
        private val nanoTime: Long
    ) {
        fun now(): Long {
            val deltaNanos = clock.nanoTime() - nanoTime
            return epochNanos + deltaNanos
        }

        companion object {
            @JvmStatic
            fun create(clock: Clock): AnchoredClock {
                return AnchoredClock(clock, clock.now(), clock.nanoTime())
            }
        }
    }
}