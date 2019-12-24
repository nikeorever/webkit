package com.allin.android.webkit.api

import androidx.lifecycle.Lifecycle

interface LifecycleRegistrant {

    fun registerWith(lifecycle: Lifecycle)
}