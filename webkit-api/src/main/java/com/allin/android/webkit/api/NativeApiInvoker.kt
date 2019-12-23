@file:JvmMultifileClass

package com.allin.android.webkit.api

/**
 * 可以中间做代理
 */
interface NativeApiInvoker {
    @Throws(Exception::class)
    fun invoke(vararg params: Any?): Any?
}

/**
 * 由于Method.parameters相关Api必须在Api Level 26以上,所以在Compile Time在APT中进行检查,
 * 选择对应的Api
 */
interface AsyncNativeApiInvoker : NativeApiInvoker