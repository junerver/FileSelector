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

    /**
     * Description: 保留给Java使用者
     * @author Junerver
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
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
            ActivityCompat.requestPermissions(activity, permissions, mRequestCode)
        } else {
            result.passPermission()
        }
    }

    /**
     * Description: dsl扩展
     * @author Junerver
     * @date: 2022/2/18-10:09
     * @Email: junerver@gmail.com
     * @Version: v1.0
     */
    fun checkPermissions(
        activity: Activity,
        permissions: Array<String>,
        result: PermissionsResultDsl.() -> Unit
    ) {
        val dsl = PermissionsResultDsl()
        dsl.result()
        checkPermissions(activity, permissions, dsl)
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
            when {
                hasPermissionDenied -> {
                    mPermissionsResult!!.continuePermission()
                }
                notRemindAgain -> {
                    mPermissionsResult!!.refusePermission()
                }
                else -> {
                    mPermissionsResult!!.passPermission()
                }
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

    class PermissionsResultDsl : PermissionsResult {
        private var passPermission: (() -> Unit)? = null
        private var continuePermission: (() -> Unit)? = null
        private var refusePermission: (() -> Unit)? = null

        fun passPermission(method: () -> Unit) {
            passPermission = method
        }

        fun continuePermission(method: () -> Unit) {
            continuePermission = method
        }

        fun refusePermission(method: () -> Unit) {
            refusePermission = method
        }

        override fun passPermission() {
            passPermission?.invoke()
        }

        override fun continuePermission() {
            continuePermission?.invoke()
        }

        override fun refusePermission() {
            refusePermission?.invoke()
        }

    }
}