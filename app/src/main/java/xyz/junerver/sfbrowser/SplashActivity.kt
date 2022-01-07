package xyz.junerver.sfbrowser

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import xyz.junerver.fileselector.*

const val REQUEST_CODE_ANDROID_DATA = 888

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }



    fun delayStart() {
        run{

        }

        let{

        }
        Handler(Looper.getMainLooper()).postDelayed({
            "延时跳转".log()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 500L)
    }

}