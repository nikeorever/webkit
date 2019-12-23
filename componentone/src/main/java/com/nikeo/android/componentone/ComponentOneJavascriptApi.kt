package com.nikeo.android.componentone

import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.allin.android.webkit.activity.WebActivity
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.annotations.JavascriptNamespace

@JavascriptNamespace(namespace = "")
class ComponentOneJavascriptApi {

    @JavascriptApi(passContentToFirstParameter = false)
    @JavascriptInterface
    @Keep
    fun callNative1(activity: WebActivity, param: String) {
        Log.i("ComponentOneJavascriptApi", "callNative1: $activity, $param")
    }
}