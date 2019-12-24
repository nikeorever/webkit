package com.allin.android.webkit.api

import kotlin.reflect.KFunction

interface NativeApiInterceptor {
    fun intercept(invoker: Invoker): Any?
}

open class DefaultNativeApiInterceptor : NativeApiInterceptor {
    override fun intercept(invoker: Invoker): Any? {
        return invoker.invoke()
    }
}

class Invoker(
    private val instance: Any,
    private val kf: KFunction<*>,
    var parameterValues: Array<out Any?>
) {
    val enclosingClass: Class<*> = instance.javaClass
    val functionName: String = kf.name

    fun invoke(): Any? {
        return kf.call(instance, *parameterValues)
    }
}