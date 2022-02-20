package com.raju.disney.opentelemetry

import java.util.regex.Pattern

internal class ServerTimingHeaderParser {
    /**
     * The first element is the trace id, the 2nd is the span id.
     *
     * @param header of the form: traceparent;desc="00-9499195c502eb217c448a68bfe0f967c-fe16eca542cd5d86-01"
     * @return A two-element array of TraceId/SpanId. An empty array if the header can't be parsed.
     *
     *
     * This will also consider single-quotes valid for delimiting the "desc" section, even though it's not to spec.
     */
    fun parse(header: String?): ArrayList<String> {
        if (header == null) {
            return UN_PARSEABLE_RESULT
        }
        val matcher = headerPattern.matcher(header)
        if (!matcher.matches()) {
            return UN_PARSEABLE_RESULT
        }
        val traceId = matcher.group(1)
        val spanId = matcher.group(2)
        return arrayListOf(traceId, spanId)
    }

    companion object {
        private val UN_PARSEABLE_RESULT = ArrayList<String>()
        private val headerPattern =
            Pattern.compile("traceparent;desc=['\"]00-([0-9a-f]{32})-([0-9a-f]{16})-01['\"]")
    }
}