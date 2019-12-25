package com.nikeo.android.componentone

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.allin.android.webkit.interceptors.LogNativeApiInterceptor
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.annotations.JavascriptNamespace

@JavascriptNamespace(namespace = "", interceptor = LogNativeApiInterceptor::class)
class ComponentOneJavascriptApi: LifecycleObserver{

    @JavascriptApi(passContextToFirstParameter = true)
    @JavascriptInterface
    @Keep
    fun callNative1(context: Context, param: String) {
        Log.i("ComponentOneJavascriptApi", "callNative1: $context, $param")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_CREATE)
    fun onCreate(source: LifecycleOwner) {
        Log.i("Main=========", "onCreate: $source")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_START)
    fun onStart(source: LifecycleOwner) {
        Log.i("Main=========", "onStart: $source")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_RESUME)
    fun onResume(source: LifecycleOwner) {
        Log.i("Main=========", "onResume: $source")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    fun onPause(source: LifecycleOwner) {
        Log.i("Main=========", "onPause: $source")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
    fun onStop(source: LifecycleOwner) {
        Log.i("Main=========", "onStop: $source")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_DESTROY)
    fun onDestroy(source: LifecycleOwner) {
        Log.i("Main=========", "onDestroy: $source")
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_ANY)
    fun onAny(source: LifecycleOwner) {
        Log.i("Main=========", "onAny: $source")
    }
}