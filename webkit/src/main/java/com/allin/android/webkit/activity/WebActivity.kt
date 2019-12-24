package com.allin.android.webkit.activity

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.allin.android.webkit.JavascriptApiRoom
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.api.NativeApiInvoker
import com.allin.android.webkit.find
import wendu.dsbridge.DWebView
import java.lang.reflect.Method

class WebActivity {

    fun checkRoom(context: AppCompatActivity) {
        val javaScriptNamespaceInterfaces = JavascriptApiRoom.javaScriptNamespaceInterfaces
        println(javaScriptNamespaceInterfaces)
        val apiInvoker = javaScriptNamespaceInterfaces[""].find("callNative1")
        val methodInfo = apiInvoker?.methodInfo()
        val methodType = methodInfo?.methodType
        val passContextToFirstParameter = methodInfo?.passContextToFirstParameter

        apiInvoker?.invoke(context, "param")

        JavascriptApiRoom.lifecycleRegistrants.forEach {
            it.registerWith(context.lifecycle)
        }
    }

    fun onCreate(activity: AppCompatActivity) {
        val webView = DWebView(activity)
        webView.addJavascriptObject("", "")
        webView.addJavascriptObject("", "")
        webView.addJavascriptObject("", "")
        webView.addJavascriptObject("", "")
        webView.addJavascriptObject("", "")
        webView.addJavascriptObject("", "")
    }

    fun checkMethod(method: Method) {
        if (!method.isAccessible) {
            method.isAccessible = true
        }
        // must annotate JavascriptApi/Keep/JavascriptInterface
        if (!method.isAnnotationPresent(JavascriptApi::class.java) ||
//            !method.isAnnotationPresent(Keep::class.java) ||
            !method.isAnnotationPresent(JavascriptInterface::class.java)
        ) {
            throw IllegalStateException(
                "${method.name} must annotated " +
                        "${JavascriptApi::class.java.canonicalName} AND " +
                        "${Keep::class.java.canonicalName} AND " +
                        "${JavascriptInterface::class.java}"
            )
        }

        // parameter must sort with(Context?, Any?, AsyncCallback?) or empty parameters
        //TODO above api 26
    }

    fun Map<Method, NativeApiInvoker>.find(methodNameFromJavascript: String?): NativeApiInvoker? {
        return filterKeys { method ->
            checkMethod(method)
            methodNameFromJavascript == method.name
        }.let { match ->
            // match size must be 0 or 1
            require(match.isEmpty() || match.size == 1) {
                "duplicate method name found in same namespace"
            }
            match.values.firstOrNull()
        }
    }


}