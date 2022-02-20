package com.raju.disney.ui.activity

import TOAST
import android.os.Bundle
import androidx.activity.viewModels
import com.raju.disney.R
import com.raju.disney.base.BaseActivity
import com.raju.disney.data.FlightData
import com.raju.disney.ui.activity.viewmodel.FlightActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_flight.*

@AndroidEntryPoint
class FlightActivity : BaseActivity() {

    private val viewModel: FlightActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight)
        val span = oTel.startWorkflow("open:FlightActivity:fetchFlightData")
        viewModel.fetchFlightData()
        viewModel.displayFlightData.observeEvent(this, this::showUser)
        viewModel.showErrorMessage.observeEvent(this, this::showErrorMessage)
        span.end()
    }

    private fun showUser(flight: FlightData) {
        val result = StringBuilder()
        flight.forEach {
            result.append(it).append("\n\n")
        }
        tvFlightData.text = result
    }


    private fun showErrorMessage(message: String?) {
        TOAST(message)
    }
}