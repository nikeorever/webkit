package com.nikeo.android.webapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.allin.android.webkit.AWebkit
import com.allin.android.webkit.MainActivity
import com.allin.android.webkit.activity.WebActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this, MainActivity::class.java))

        AWebkit.init(this)

        WebActivity().checkRoom(this)

        Log.i("CountDownLatch", "start--------")
        val latch = CountDownLatch(3)
        latch.await(10, TimeUnit.SECONDS)
        Log.i("CountDownLatch", "end--------")
    }


}
