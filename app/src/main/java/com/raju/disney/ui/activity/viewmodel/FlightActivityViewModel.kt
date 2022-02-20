package com.raju.disney.ui.activity.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.raju.disney.api.repository.FlightRepository
import com.raju.disney.base.BaseViewModel
import com.raju.disney.data.FlightData
import com.raju.disney.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.StatusCode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG: String = "FlightActivityViewModel"

@HiltViewModel
class FlightActivityViewModel @Inject constructor(private val repository: FlightRepository) :
    BaseViewModel() {

    private val loadingObservableField: ObservableField<Boolean> = ObservableField()
    val showErrorMessage: SingleLiveEvent<String?> by lazy { SingleLiveEvent() }
    val displayFlightData: SingleLiveEvent<FlightData> by lazy { SingleLiveEvent() }

    fun fetchFlightData() {
        viewModelScope.launch {
            val span = otel.startWorkflow("fetchFlightData:http:request")
            loadingObservableField.set(true)
            repository.getFlightData(otel.injectSpanContext())
                .catch { e ->
                    handleException(TAG, e, loadingObservableField)
                    val attributes = Attributes.of(
                        AttributeKey.stringKey(StatusCode.ERROR.name),
                        "/@GET/flight"
                    )
                    otel.addDisneyOtelException(e, attributes)
                }
                .collect {
                    displayFlightData.value = it
                    loadingObservableField.set(false)
                    span.addEvent("Api data loaded: $it")
                }
            span.end()
        }
    }

    override fun showExceptionMessage(message: String?) {
        showErrorMessage.value = message
    }
}