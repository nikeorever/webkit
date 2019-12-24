@file:JvmName("MethodChecker")
@file:JvmMultifileClass


package com.allin.android.webkit.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.api.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

@RestrictTo(value = [RestrictTo.Scope.LIBRARY_GROUP_PREFIX])
fun functionInvokerMappersOf(instance: Any, supplier: FunctionInvokerMappersSupplier) {
    val members = instance::class.members
    members.filter { it is KFunction && it.hasAnnotation<JavascriptApi>()}.map {
        val kFunction = it as KFunction
        if (!kFunction.isAccessible) {
            kFunction.isAccessible = true
        }
        val javascriptApi = kFunction.findAnnotation<JavascriptApi>()!!
        val valueParameters = kFunction.valueParameters
        if (javascriptApi.passContextToFirstParameter) {
            //Context, Any, AsyncCallback
            //Context, Any

            // check parameters
            runCatching {
                when (valueParameters.size) {
                    2 -> {
                        valueParameters[0].type === Context::class
                        valueParameters[1].type !== AsyncCallback::class

                        MethodInfo(methodType = MethodType.SYNC, passContextToFirstParameter = true)
                    }
                    3 -> {
                        valueParameters[0].type === Context::class
                        valueParameters[1].type !== AsyncCallback::class
                        valueParameters[2].type === AsyncCallback::class

                        MethodInfo(methodType = MethodType.ASYNC, passContextToFirstParameter = true)
                    }
                    else -> {
                        val errorMessage = ""
                        throw NoSuchMethodException("")
                    }
                }
            }.onFailure {
                throw NoSuchMethodException("")
            }.map { methodInfo ->
                kFunction to object :NativeApiInvoker {
                    override fun invoke(vararg params: Any?): Any? {
                        return kFunction.call(instance, *params)
                    }
                    override fun methodInfo(): MethodInfo = methodInfo
                }
            }.getOrThrow()
        } else {
            //Any, AsyncCallback
            //Any

            // check parameters
            runCatching {
                when (valueParameters.size) {
                    1 -> {
                        valueParameters[0].type !== AsyncCallback::class

                        MethodInfo(methodType = MethodType.SYNC, passContextToFirstParameter = false)
                    }
                    2 -> {
                        valueParameters[0].type !== AsyncCallback::class
                        valueParameters[1].type === AsyncCallback::class

                        MethodInfo(methodType = MethodType.ASYNC, passContextToFirstParameter = false)
                    }
                    else -> {
                        val errorMessage = ""
                        throw NoSuchMethodException("")
                    }
                }
            }.onFailure {
                throw NoSuchMethodException("")
            }.map { methodInfo ->
                kFunction to object :NativeApiInvoker {
                    override fun invoke(vararg params: Any?): Any? {
                        return kFunction.call(instance, *params)
                    }
                    override fun methodInfo(): MethodInfo = methodInfo
                }
            }.getOrThrow()
        }
    }.apply {
        if (isNotEmpty()) {
            supplier.get(toMap())
        }
    }
}