package com.nikeo.android.webapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.allin.android.webkit.MainActivity
import com.allin.android.webkit.activity.WebActivity
import com.allin.android.webkit.annotations.JavascriptApi
import com.allin.android.webkit.api.FunctionInvokerMappersSupplier
import com.allin.android.webkit.api.NativeApiInvoker
import com.allin.android.webkit.init
import com.allin.android.webkit.internal.functionInvokerMappersOf
import com.nikeo.android.componentone.ComponentOneJavascriptApi
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this, MainActivity::class.java))

        init(this)

        WebActivity().checkRoom(this)
    }


}
