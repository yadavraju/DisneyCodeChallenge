package com.raju.disney.base

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.raju.disney.opentelemetry.DisneyOTel
import com.raju.disney.util.ApiExceptionUtils.getExceptionMessage

open class BaseViewModel : ViewModel() {

    val otel = DisneyOTel.instance

    protected fun handleException(
        TAG: String,
        e: Throwable,
        loadingObservableField: ObservableField<Boolean>
    ) {
        Log.e(TAG, """${e.message}""")
        loadingObservableField.set(false)
        showExceptionMessage(getExceptionMessage(e))
    }

    open fun showExceptionMessage(message: String?) {}
}
