package com.allin.android.webkit.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JavascriptApi(val passContentToFirstParameter: Boolean)