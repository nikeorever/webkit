@file:JvmName("AWebkit")

package com.allin.android.webkit

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.allin.android.webkit.api.*
import com.allin.android.webkit.internal.listClassesInPackageOf
import kotlin.reflect.KFunction

object AWebkit {

    private val scannerRepository = ScannerRepository()

    fun init(context: Context) {
        val ignoredKey = "ignoredIndex"
        listClassesInPackageOf(context, GENERATED_PACKAGE_NAME)?.groupBy {
            when {
                it.endsWith(SUFFIX_JS_COLLECTOR) -> SUFFIX_JS_COLLECTOR
                it.endsWith(SUFFIX_LIFECYCLE_REGISTRANT) -> SUFFIX_LIFECYCLE_REGISTRANT
                else -> ignoredKey
            }
        }?.apply {
            this[SUFFIX_JS_COLLECTOR].takeUnless { it.isNullOrEmpty() }?.forEach { fileName ->
                val collector =
                    Class.forName(fileName).getConstructor().newInstance() as JavascriptApiCollector
                val namespace = collector.namespace()

                scannerRepository.javaScriptNamespaceInterfaces
                    .getOrPut(namespace) { mutableMapOf() }
                    .also {
                        collector.collectTo(it)
                    }
            }

            this[SUFFIX_LIFECYCLE_REGISTRANT].takeUnless { it.isNullOrEmpty() }
                ?.forEach { fileName ->
                    val registrant =
                        Class.forName(fileName).getConstructor().newInstance() as LifecycleRegistrant

                    scannerRepository.lifecycleRegistrants.add(registrant)
                }
        }
    }

    fun registerLifecycle(lifecycle: Lifecycle) {
        scannerRepository.registerLifecycle(lifecycle)
    }

    fun findNativeInvoker(namespace: String, methodNameFromJavascript: String): NativeApiInvoker? {
        return scannerRepository.findNativeInvoker(namespace, methodNameFromJavascript)
    }

    class ScannerRepository {
        /*
            namespace           method        nativeApiInvoker
            1                    #1           &1
                                 #2           &2
                                 #3           &3
                                 #4           &4
                                 #5           &5


            2
        */
        val javaScriptNamespaceInterfaces: MutableMap<String, MutableMap<KFunction<*>, NativeApiInvoker>> =
            mutableMapOf()
        val lifecycleRegistrants: MutableList<LifecycleRegistrant> = arrayListOf()

        fun registerLifecycle(lifecycle: Lifecycle) {
            lifecycleRegistrants.forEach {
                it.registerWith(lifecycle)
            }
        }

        fun findNativeInvoker(
            namespace: String,
            methodNameFromJavascript: String
        ): NativeApiInvoker? {
            return javaScriptNamespaceInterfaces[namespace].find(methodNameFromJavascript)
        }

        private fun Map<KFunction<*>, NativeApiInvoker>?.find(methodNameFromJavascript: String?): NativeApiInvoker? {
            return this?.filterKeys { method ->
                methodNameFromJavascript == method.name
            }?.let { match ->
                // match size must be 0 or 1
                require(match.isEmpty() || match.size == 1) {
                    "duplicate method name found in same namespace"
                }
                match.values.firstOrNull()
            }
        }
    }
}
