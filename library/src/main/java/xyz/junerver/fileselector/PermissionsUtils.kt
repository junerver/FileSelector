package xyz.junerver.fileselector

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Description:
 * @author Junerver
 * date: 2022/2/17-17:21
 * Email: junerver@gmail.com
 * Version: v1.0
 */
object PermissionsUtils {
    private const val mRequestCode = 100
    private var mPermissionsResult: PermissionsResult? = null

    fun checkPermissions(
        activity: Activity,
        permissions: Array<String>,
        result: PermissionsResult
    ) {
        mPermissionsResult = result
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            result.passPermission()
            return
        }
        val mPermissionList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissionList.add(permission)
            }
        }
        if (mPermissionList.size > 0) {
            ActivityCompat.requestPermissions(activity!!, permissions, mRequestCode)
        } else {
            result.passPermission()
        }
    }

    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        var hasPermissionDenied = false
        var notRemindAgain = false
        if (mRequestCode == requestCode) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    val flag = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        permissions[i]
                    )
                    if (flag) {
                        hasPermissionDenied = true
                    } else {
                        notRemindAgain = true
                    }
                }
            }
            if (hasPermissionDenied) {
                mPermissionsResult!!.continuePermission()
            } else if (notRemindAgain) {
                mPermissionsResult!!.refusePermission()
            } else {
                mPermissionsResult!!.passPermission()
            }
        }
    }

    interface PermissionsResult {
        /**
         * 权限全部通过
         */
        fun passPermission()

        /**
         * 权限部分通过
         */
        fun continuePermission()

        /**
         * 权限拒绝且不再提醒
         */
        fun refusePermission()
    }
}