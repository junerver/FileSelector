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
        val context = this
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
                                //请求文件管理权限
                                startActivityForResult(
                                    it,
                                    REQUEST_CODE_MANAGE_APP_ALL_FILES
                                )
                            }
                        )
                    } else {
                        //获取权限成功
                        val grant = FileUriUtils.isGrant(context)
                        "是否获得data权限: $grant".log()
                        if (!grant) {
                            //请求data权限
                            showRequestDataTips(
                                cancel = {
                                    toast("未获得Android/data目录权限，无法浏览该目录下文件！")
                                    delayStart()
                                },
                                request = {
                                    FileUriUtils.startForRoot(context, REQUEST_CODE_ANDROID_DATA)
                                }
                            )
                        } else {
                            delayStart()
                        }
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

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MANAGE_APP_ALL_FILES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                toast("文件访问权限获取失败，部分文件可能无法展示！")
                delayStart()
            } else {
                //获取权限成功
                val grant = FileUriUtils.isGrant(this)
                "是否获得data权限: $grant".log()
                if (!grant) {
                    //请求data权限
                    showRequestDataTips(
                        cancel = {
                            toast("未获得Android/data目录权限，无法浏览该目录下文件！")
                            delayStart()
                        },
                        request = {
                            FileUriUtils.startForRoot(this, REQUEST_CODE_ANDROID_DATA)
                        }
                    )
                } else {
                    delayStart()
                }
            }

        } else if (requestCode == REQUEST_CODE_ANDROID_DATA) {
            //关键是这里，这个就是保存这个目录的访问权限
            data?.let {
                it.data?.let {uri->
                    contentResolver.takePersistableUriPermission(uri, data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                }
            }
            val grant = FileUriUtils.isGrant(this)
            "是否获得data权限: $grant".log()
            if (!grant) {
                //请求data权限
                toast("未获得Android/data目录权限，无法浏览该目录下文件！")
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