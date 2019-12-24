package com.allin.android.webkit.api.internal

import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.api.AsyncCallback
import java.lang.reflect.Method
import java.util.*

fun checkMethod(cls: Class<*>, method: Method) {
    if (!method.isAccessible) {
        method.isAccessible = true
    }
    if (method.isAnnotationPresent(JavascriptApi::class.java)) {
        val javascriptApi = method.getAnnotation(JavascriptApi::class.java)
        if (javascriptApi.passContextToFirstParameter) {
            try {
                cls.getDeclaredMethod(
                    method.name, android.content.Context::class.java, Any::class.java,
                    AsyncCallback::class.java
                )
                // async method found
            } catch (ignored: NoSuchMethodException) {
                try {
                    cls.getDeclaredMethod(
                        method.name,
                        android.content.Context::class.java,
                        Any::class.java
                    )
                    // sync method found
                } catch (ex: NoSuchMethodException) { // illegal method found
                    throw IllegalArgumentException(
                        String.format(
                            Locale.getDefault(),
                            "method(%s) parameterTypes must be (%s, %s, %s) or (%s, %s)",
                            method.name,
                            android.content.Context::class.java,
                            Any::class.java,
                            AsyncCallback::class.java,
                            android.content.Context::class.java,
                            Any::class.java
                        )
                    )
                }
            }
        } else {

        }
    } else {
        throw IllegalStateException(
            "${method.name} must annotated ${JavascriptApi::class.java.canonicalName}"
        )
    }
}