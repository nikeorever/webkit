package com.allin.android.webkit.annotations

import com.allin.android.webkit.api.DefaultNativeApiInterceptor
import com.allin.android.webkit.api.NativeApiInterceptor
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class JavascriptNamespace(
    val namespace: String,
    val interceptor: KClass<out NativeApiInterceptor> = DefaultNativeApiInterceptor::class
)