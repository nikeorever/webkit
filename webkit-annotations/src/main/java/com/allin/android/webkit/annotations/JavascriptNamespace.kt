package com.allin.android.webkit.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class JavascriptNamespace(val namespace: String)