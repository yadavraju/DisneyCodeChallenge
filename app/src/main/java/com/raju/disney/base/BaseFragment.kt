package com.raju.disney.base

import androidx.fragment.app.Fragment
import com.raju.disney.opentelemetry.DisneyOTel

abstract class BaseFragment : Fragment() {
    val otel = DisneyOTel.instance
}
