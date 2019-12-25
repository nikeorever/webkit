package com.allin.android.webkit.activity

import androidx.appcompat.app.AppCompatActivity
import com.allin.android.webkit.AWebkit

class WebActivity {

    fun checkRoom(context: AppCompatActivity) {

        AWebkit.registerLifecycle(context.lifecycle)
        AWebkit.findNativeInvoker("", "callNative1")?.invoke(context, "param")

//        val javaScriptNamespaceInterfaces = JavascriptApiRoom.javaScriptNamespaceInterfaces
//        println(javaScriptNamespaceInterfaces)
//        val apiInvoker = javaScriptNamespaceInterfaces[""].find("callNative1")
//        val methodInfo = apiInvoker?.methodInfo()
//        val methodType = methodInfo?.methodType
//        val passContextToFirstParameter = methodInfo?.passContextToFirstParameter
//
//        apiInvoker?.invoke(context, "param")
//
//        JavascriptApiRoom.lifecycleRegistrants.forEach {
//            it.registerWith(context.lifecycle)
//        }
    }
}