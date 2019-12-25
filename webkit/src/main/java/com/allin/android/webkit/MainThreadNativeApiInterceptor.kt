package com.allin.android.webkit

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.allin.android.webkit.api.DefaultNativeApiInterceptor
import com.allin.android.webkit.api.Invoker


class MainThreadNativeApiInterceptor : DefaultNativeApiInterceptor() {

    private var mMainHandler: Handler? = null
    private val mLock: Any = Any()

    override fun intercept(invoker: Invoker): Any? {
        if (!isMainThread()) {
            postToMainThread {
                super.intercept(invoker)
            }
        }
        return null
    }

    private fun postToMainThread(runnable: () -> Unit) {
        if (mMainHandler == null) {
            synchronized(mLock) {
                if (mMainHandler == null) {
                    mMainHandler = createAsync(Looper.getMainLooper())
                }
            }
        }
        mMainHandler!!.post(runnable)
    }

    private fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread == Thread.currentThread()
    }

    companion object {
        @SuppressLint("ObsoleteSdkInt")
        private fun createAsync(looper: Looper): Handler {
            if (Build.VERSION.SDK_INT >= 28) {
                return Handler.createAsync(looper)
            }

            if (Build.VERSION.SDK_INT >= 16) {
                return runCatching {
                    Handler::class.java.getDeclaredConstructor(
                        Looper::class.java,
                        Handler.Callback::class.java,
                        Boolean::class.java
                    ).newInstance(looper, null, true)
                }.getOrElse {
                    Handler(looper)
                }
            }
            return Handler(looper)
        }
    }
}