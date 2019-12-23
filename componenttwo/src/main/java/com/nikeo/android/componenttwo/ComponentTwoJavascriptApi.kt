package com.nikeo.android.componenttwo

import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.annotations.JavascriptNamespace

@Keep
@JavascriptNamespace(namespace = "")
class ComponentTwoJavascriptApi {

    @JavascriptApi(passContentToFirstParameter = true)
    @Keep
    @JavascriptInterface
    fun callNative2() {
        Log.i("ComponentTwoJavascriptApi", "callNative2")
    }
}