package com.raju.disney.opentelemetry

import android.util.Log
import com.raju.disney.opentelemetry.DisneyOTel.Companion.LINK_SPAN_ID_KEY
import com.raju.disney.opentelemetry.DisneyOTel.Companion.LINK_TRACE_ID_KEY
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.AttributesBuilder
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import okhttp3.Request
import okhttp3.Response

internal class ResponseAttributesExtractor(private val serverTimingHeaderParser: ServerTimingHeaderParser) :
    AttributesExtractor<Request?, Response?> {

    override fun onStart(attributes: AttributesBuilder, request: Request?) {
        attributes.put(DisneyOTel.COMPONENT_KEY, "http")
    }

    override fun onEnd(
        attributes: AttributesBuilder,
        request: Request?,
        response: Response?,
        error: Throwable?
    ) {
        response?.let { onResponse(attributes, it) }
        error?.let { onError(attributes, it) }
    }

    private fun onResponse(attributes: AttributesBuilder, response: Response) {
        recordContentLength(attributes, response)
        Log.e("Raju", "Response : ${response.headers}")
        //We can get response header from api call
        val serverTimingHeader = response.header("Server-Timing")
        val ids: ArrayList<String> = serverTimingHeaderParser.parse(serverTimingHeader)
        if (ids.size == 2) {
            attributes.put(LINK_TRACE_ID_KEY, ids[0])
            attributes.put(LINK_SPAN_ID_KEY, ids[1])
        }
    }

    private fun recordContentLength(attributesBuilder: AttributesBuilder, response: Response) {
        //make a best low-impact effort at getting the content length on the response.
        val contentLengthHeader = response.header("Content-Length")
        if (contentLengthHeader != null) {
            try {
                val contentLength = contentLengthHeader.toLong()
                if (contentLength > 0) {
                    attributesBuilder.put(
                        SemanticAttributes.HTTP_RESPONSE_CONTENT_LENGTH,
                        contentLength
                    )
                }
            } catch (e: NumberFormatException) {
                //who knows what we got back? It wasn't a number!
            }
        }
    }

    private fun onError(attributes: AttributesBuilder, error: Throwable) {
        DisneyOTel.addExceptionAttributes({ key, value ->
            attributes.put(
                key as AttributeKey<in Any?>?,
                value
            )
        }, error)
    }

}