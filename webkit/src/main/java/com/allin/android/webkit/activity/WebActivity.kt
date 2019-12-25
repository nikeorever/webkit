package com.allin.android.webkit.activity

import androidx.appcompat.app.AppCompatActivity
import com.allin.android.webkit.AWebkit
import com.allin.android.webkit.api.AsyncCallback
import com.allin.android.webkit.api.MethodType

class WebActivity {

    fun checkRoom(context: AppCompatActivity) {

        AWebkit.registerLifecycle(context.lifecycle)
        AWebkit.findNativeInvoker("", "callNative1")?.apply {
            val (methodType, passContextToFirstParameter) = methodInfo()
            when (methodType) {
                MethodType.SYNC -> {
                    if (passContextToFirstParameter) {
                        invoke(context, "param")
                    } else {
                        invoke("param")
                    }
                }
                MethodType.ASYNC -> {
                    if (passContextToFirstParameter) {
                        invoke(context, "param", object : AsyncCallback {
                            override fun call(value: Any?, complete: Boolean) {
                            }
                        })
                    } else {
                        invoke("param", object : AsyncCallback {
                            override fun call(value: Any?, complete: Boolean) {
                            }
                        })
                    }
                }
            }
        }
    }
}