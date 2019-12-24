package com.allin.android.webkit

import com.allin.android.webkit.api.LifecycleRegistrant
import com.allin.android.webkit.api.NativeApiInvoker
import java.lang.reflect.Method
import kotlin.reflect.KFunction

object JavascriptApiRoom {

    /*
        namespace           method        nativeApiInvoker
        1                    #1           &1
                             #2           &2
                             #3           &3
                             #4           &4
                             #5           &5


        2
     */
    val javaScriptNamespaceInterfaces: MutableMap<String, MutableMap<KFunction<*>, NativeApiInvoker>> = mutableMapOf()
    val lifecycleRegistrants: MutableList<LifecycleRegistrant> = arrayListOf()
}

fun Map<KFunction<*>, NativeApiInvoker>?.find(methodNameFromJavascript: String?): NativeApiInvoker? {
    return this?.filterKeys { method ->
        methodNameFromJavascript == method.name
    }?.let { match ->
        // match size must be 0 or 1
        require(match.isEmpty() || match.size == 1) {
            "duplicate method name found in same namespace"
        }
        match.values.firstOrNull()
    }
}