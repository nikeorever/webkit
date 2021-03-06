package com.allin.android.webkit.interceptors

import android.util.Log
import com.allin.android.webkit.BuildConfig
import com.allin.android.webkit.api.DefaultNativeApiInterceptor
import com.allin.android.webkit.api.Invoker

private const val TAG = "AWebkit"

class LogNativeApiInterceptor : DefaultNativeApiInterceptor() {
    override fun intercept(invoker: Invoker): Any? {
        if (BuildConfig.DEBUG) {
            Log.i(
                TAG, "JavascriptApiClass: ${invoker.enclosingClass},\n" +
                        "FunctionName: ${invoker.functionName},\n" +
                        "ParameterValues: ${invoker.parameterValues.joinToString()}") }
        return super.intercept(invoker)
    }
}