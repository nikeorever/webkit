package com.nikeo.android.webapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.allin.android.webkit.MainActivity
import com.allin.android.webkit.activity.WebActivity
import com.allin.android.webkit.init

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this, MainActivity::class.java))

        init(this)

        WebActivity().checkRoom()
    }
}
