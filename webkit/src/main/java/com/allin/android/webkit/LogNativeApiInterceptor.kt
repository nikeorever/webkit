package com.allin.android.webkit

import android.util.Log
import com.allin.android.webkit.api.DefaultNativeApiInterceptor
import com.allin.android.webkit.api.Invoker

class LogNativeApiInterceptor : DefaultNativeApiInterceptor() {
    override fun intercept(invoker: Invoker): Any? {
        if (BuildConfig.DEBUG) {
            Log.i(
                "LogNativeApiInterceptor",
                "JavascriptApiClass: ${invoker.enclosingClass},\n" +
                        "FunctionName: ${invoker.functionName},\n" +
                        "ParameterValues: ${invoker.parameterValues.joinToString()}"
            )
        }
        return super.intercept(invoker)
    }
}