package com.allin.android.webkit.api

import androidx.annotation.Keep
import java.lang.reflect.Method

@Keep
interface JavascriptApiCollector {
    @Keep
    fun collectTo(container: MutableMap<Method, NativeApiInvoker>)

    @Keep
    fun namespace(): String
}