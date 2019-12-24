package com.allin.android.webkit

import android.content.Context
import com.allin.android.webkit.api.*

fun init(context: Context) {
    val fileNames = ClassUtils.getFileNameByPackageName(context, GENERATED_PACKAGE_NAME)
    fileNames.filter { it.endsWith(SUFFIX_JS_COLLECTOR) }.forEach { fileName ->
        val collector =
            Class.forName(fileName).getConstructor().newInstance() as JavascriptApiCollector
        val namespace = collector.namespace()

        JavascriptApiRoom.javaScriptNamespaceInterfaces
            .getOrPut(namespace) { mutableMapOf() }
            .also {
                collector.collectTo(it)
            }

    }

    fileNames.filter { it.endsWith(SUFFIX_LIFECYCLE_REGISTRANT) }.forEach { fileName ->
        val registrant =
            Class.forName(fileName).getConstructor().newInstance() as LifecycleRegistrant

        JavascriptApiRoom.lifecycleRegistrants.add(registrant)
    }

}