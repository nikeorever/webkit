package com.allin.android.webkit.api

import kotlin.reflect.KFunction

interface FunctionInvokersSupplier {
    fun get(mapper: Map<KFunction<*>, NativeApiInvoker>)
}