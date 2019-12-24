package com.allin.android.webkit.api

import androidx.annotation.RestrictTo
import kotlin.reflect.KFunction

@RestrictTo(value = [RestrictTo.Scope.LIBRARY_GROUP_PREFIX])
interface FunctionInvokerMappersSupplier {
    fun get(mapper: Map<KFunction<*>, NativeApiInvoker>)
}