package com.raju.disney.base

import androidx.fragment.app.Fragment
import com.raju.disney.opentelemetry.DisneyOtel

abstract class BaseFragment : Fragment() {
    val otel = DisneyOtel.getInstance()
}
