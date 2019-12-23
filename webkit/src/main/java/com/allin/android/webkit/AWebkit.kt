package com.allin.android.webkit

import android.content.Context
import com.allin.android.webkit.api.GENERATED_PACKAGE_NAME
import com.allin.android.webkit.api.JavascriptApiCollector

fun init(context: Context) {
    ClassUtils.getFileNameByPackageName(context, GENERATED_PACKAGE_NAME)
        .filter { it.endsWith("\$JsApiCollector") }.forEach { fileName ->
            val collector =
                Class.forName(fileName).getConstructor().newInstance() as JavascriptApiCollector
            val namespace = collector.namespace()

            JavascriptApiRoom.javaScriptNamespaceInterfaces
                .getOrPut(namespace) { mutableMapOf() }
                .also {
                    collector.collectTo(it)
                }

        }

}