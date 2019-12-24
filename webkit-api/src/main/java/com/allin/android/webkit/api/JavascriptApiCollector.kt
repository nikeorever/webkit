package com.allin.android.webkit.api

import androidx.annotation.Keep
import kotlin.reflect.KFunction

@Keep
interface JavascriptApiCollector {
    @Keep
    fun collectTo(container: MutableMap<KFunction<*>, NativeApiInvoker>)

    @Keep
    fun namespace(): String
}