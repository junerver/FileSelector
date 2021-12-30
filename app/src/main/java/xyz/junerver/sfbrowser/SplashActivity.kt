package xyz.junerver.sfbrowser

import android.Manifest
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import xyz.junerver.fileselector.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        PermissionsUtils.getInstance()
            .checkPermissions(this, permissions, object : PermissionsUtils.PermissionsResult {
                override fun passPermission() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        "没有文件管理权限去申请".log()
                        showManagerFileTips(
                            cancel = { delayStart() },
                            request = {
                                startActivityForResult(
                                    it,
                                    REQUEST_CODE_MANAGE_APP_ALL_FILES
                                )
                            }
                        )
                    } else {
                        delayStart()
                    }
                }

                override fun continuePermission() {
                    toast("读写权限被拒绝")
                }

                override fun refusePermission() {
                    toast("读写权限被拒绝")
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.getInstance()
            .onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_APP_ALL_FILES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                toast("文件访问权限获取失败，部分文件可能无法展示！")
            }
            delayStart()
        }
    }

    fun delayStart() {
        Handler(Looper.getMainLooper()).postDelayed({
            "延时跳转".log()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 500L)
    }

}