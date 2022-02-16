package com.raju.disney.appstartup

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.raju.disney.opentelemetry.OtelConfiguration
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer

class StartupTimeProvider : ContentProvider() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val tracer: Tracer = OtelConfiguration.getTracer("app:startup")

    companion object {
        private val TAG = StartupTimeProvider::class.simpleName
    }

    override fun onCreate(): Boolean {
        val span = tracer.spanBuilder("cold_startup_time").setSpanKind(SpanKind.CLIENT).startSpan();
        try {
            span?.makeCurrent().use {
                span?.setAttribute("StartupTimeProvider:onCreate", "onCreate")
                StartupTrace.onColdStartInitiated(context!!)
                mainHandler.post(StartupTrace.StartFromBackgroundRunnable)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize StartupTimeProvider", e)
            span.setStatus(StatusCode.ERROR, "Failed to initialize StartupTimeProvider");
            span.recordException(e.cause!!)
        } finally {
            span?.end()
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}