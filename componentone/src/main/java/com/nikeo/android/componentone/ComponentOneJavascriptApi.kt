package com.nikeo.android.componentone

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.allin.android.webkit.activity.WebActivity
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.annotations.JavascriptNamespace

@JavascriptNamespace(namespace = "")
class ComponentOneJavascriptApi {

    @JavascriptApi(passContextToFirstParameter = true)
    @JavascriptInterface
    @Keep
    fun callNative1(context: Context, param: String) {
        Log.i("ComponentOneJavascriptApi", "callNative1: $context, $param")
    }
}