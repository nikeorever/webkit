package com.allin.android.webkit.api

/**
 * 可以中间做代理
 */
interface NativeApiInvoker {
    @Throws(Exception::class)
    fun invoke(vararg params: Any?): Any?

    fun methodInfo(): MethodInfo
}