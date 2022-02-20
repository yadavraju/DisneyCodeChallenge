package com.raju.disney.opentelemetry

import android.app.Application
import android.os.Looper
import android.util.Log
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapSetter
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTracing
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.function.BiConsumer


open class DisneyOTel internal constructor(
    private val openTelemetrySdk: OpenTelemetrySdk,
    private val sessionId: SessionId,
    private val config: Config
) {
    /**
     * Get a handle to the instance of the OpenTelemetry API that this instance is using for instrumentation.
     */
    open val openTelemetry: OpenTelemetry = openTelemetrySdk

    open val tracer: Tracer = openTelemetrySdk.getTracer(DISNEY_TRACER_NAME)

    private val textMapPropagator = openTelemetrySdk.propagators.textMapPropagator

    /**
     * Note: this value can change throughout the lifetime of an application instance, so it
     * is recommended that you do not cache this value, but always retrieve it from here when
     * needed.
     */
    val oTelSessionId: String = sessionId.sessionId

    /**
     * Add a custom event to Disney Otel monitoring. This can be useful to capture business events, or
     * simply add instrumentation to your application.
     *
     *
     * This event will be turned into a Span and sent to the disney ingest along with other, auto-generated
     * spans.
     *
     * @param name       The name of the event.
     * @param attributes Any [Attributes] to associate with the event.
     */
    fun addEvent(name: String, attributes: Attributes) {
        tracer
            .spanBuilder(name)
            .setAllAttributes(attributes)
            .startSpan()
            .end()
    }

    /**
     * Start a Span to time a named workflow.
     *
     * @param workflowName The name of the workflow to start.
     * @return A [Span] that has been started.
     */
    fun startWorkflow(workflowName: String): Span {
        return tracer
            .spanBuilder(workflowName)
            .setAttribute(WORKFLOW_NAME_KEY, workflowName)
            .startSpan()
    }

    /**
     * Start a Span to time a named workflow.
     *
     * @param workflowName The name of the workflow to start.
     * @return A [Span] that has been started.
     */
    fun startChildWorkflow(workflowName: String, parentSpan: Span): Span {
        return tracer
            .spanBuilder(workflowName)
            .setParent(parentSpan.storeInContext(Context.current()))
            .setAttribute(WORKFLOW_NAME_KEY, workflowName)
            .startSpan()
    }

    fun injectSpanContext(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val textMapSetter = TextMapSetter<HashMap<String, String>> { carrier, key, value ->
            carrier?.set(
                key,
                value
            )
        }
        textMapPropagator?.inject(Context.current(), map, textMapSetter)
        return map
    }
    /**
     * Add a custom exception to Disney otel monitoring. This can be useful for tracking custom error
     * handling in your application.
     *
     *
     * This event will be turned into a Span and sent to the Disney ingest along with other, auto-generated
     * spans.
     *
     * @param throwable  A [Throwable] associated with this event.
     * @param attributes Any [Attributes] to associate with the event.
     */
    /**
     * Add a custom exception to Disney otel monitoring. This can be useful for tracking custom error
     * handling in your application.
     *
     *
     * This event will be turned into a Span and sent to the Disney ingest along with other, auto-generated
     * spans.
     *
     * @param throwable A [Throwable] associated with this event.
     */
    fun addException(throwable: Throwable, attributes: Attributes = Attributes.empty()) {
        val span = tracer
            .spanBuilder(throwable.javaClass.simpleName)
            .setAllAttributes(attributes)
            .setAttribute(COMPONENT_KEY, COMPONENT_ERROR)
            .startSpan()
        addExceptionAttributes(span, throwable)
        span.end()
    }

    fun recordAnr(stackTrace: Array<StackTraceElement>) {
        tracer
            .spanBuilder("ANR")
            .setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, formatStackTrace(stackTrace))
            .setAttribute(COMPONENT_KEY, COMPONENT_ERROR)
            .startSpan()
            .setStatus(StatusCode.ERROR)
            .end()
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val stringBuilder = StringBuilder()
        for (stackTraceElement in stackTrace) {
            stringBuilder.append(stackTraceElement).append("\n")
        }
        return stringBuilder.toString()
    }

    /**
     * Wrap the provided [OkHttpClient] with OpenTelemetry and instrumentation. Since
     * [Call.Factory] is the primary useful interface implemented by the OkHttpClient, this
     * should be a drop-in replacement for any usages of OkHttpClient.
     *
     * @param client The [OkHttpClient] to wrap with OpenTelemetry and instrumentation.
     * @return A [okhttp3.Call.Factory] implementation.
     */
    open fun createRumOkHttpCallFactory(client: OkHttpClient): Call.Factory {
        return createOkHttpTracing().newCallFactory(client)
    }

    private fun createOkHttpTracing(): OkHttpTracing {
        return OkHttpTracing
            .builder(openTelemetrySdk)
            .addAttributesExtractor(ResponseAttributesExtractor(ServerTimingHeaderParser()))
            .build()
    }

    companion object {

        private val startupTimer by lazy { AppStartupTimer() }

        val COMPONENT_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("component") }
        val SCREEN_NAME_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("screen.name") }
        val LAST_SCREEN_NAME_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("last.screen.name") }
        val WORKFLOW_NAME_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("workflow.name") }
        val START_TYPE_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("start.type") }
        val LINK_TRACE_ID_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("link.traceId") }
        val LINK_SPAN_ID_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("link.spanId") }

        private val ERROR_TYPE_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("error.type") }
        private val ERROR_MESSAGE_KEY: AttributeKey<String> by lazy { AttributeKey.stringKey("error.message") }

        const val COMPONENT_APP_START = "appstart"
        const val COMPONENT_CRASH = "crash"
        const val COMPONENT_ERROR = "error"
        const val COMPONENT_UI = "ui"
        private const val LOG_TAG = "DisneyOTel"
        const val DISNEY_TRACER_NAME = "DisneyOTel"

        private lateinit var INSTANCE: DisneyOTel

        fun newConfigBuilder(): Config.Builder {
            return Config.builder()
        }

        fun initialize(config: Config, application: Application): DisneyOTel {
            if (::INSTANCE.isInitialized) {
                Log.w(LOG_TAG, "Singleton Disney Otel instance has already been initialized.")
                return INSTANCE
            }

            INSTANCE = OTelInitializer(
                config,
                application,
                startupTimer
            ).initialize(Looper.getMainLooper())

            if (config.isDebugEnabled) {
                Log.i(LOG_TAG, "Disney OTel initialized with session ID: " + INSTANCE.sessionId)
            }
            return INSTANCE
        }

        /**
         * Get the singleton instance of this class.
         */
        val instance: DisneyOTel by lazy { INSTANCE }

        fun addExceptionAttributes(span: Span, e: Throwable) {
            addExceptionAttributes({ key: AttributeKey<*>?, value: Any? ->
                span.setAttribute(
                    key as AttributeKey<in Any?>,
                    value
                )
            }, e)
        }

        fun addExceptionAttributes(setAttribute: BiConsumer<AttributeKey<*>?, Any?>, e: Throwable) {
            setAttribute.accept(SemanticAttributes.EXCEPTION_TYPE, e.javaClass.simpleName)
            setAttribute.accept(SemanticAttributes.EXCEPTION_MESSAGE, e.message)
            setAttribute.accept(ERROR_TYPE_KEY, e.javaClass.simpleName)
            setAttribute.accept(ERROR_MESSAGE_KEY, e.message)
        }
    }
}