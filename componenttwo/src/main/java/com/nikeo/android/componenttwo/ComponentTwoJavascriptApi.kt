package com.nikeo.android.componenttwo

import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.annotations.JavascriptNamespace

@Keep
@JavascriptNamespace(namespace = "")
class ComponentTwoJavascriptApi{

    @JavascriptApi(passContextToFirstParameter = false)
    @Keep
    @JavascriptInterface
    fun callNative2(param: String) {
        Log.i("CompTwoJavascriptApi", "callNative2")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
    fun onActivityResume(owner: LifecycleOwner) {
        Log.i("CompTwoJavascriptApi", "onActivityResume: $owner")
    }
}