package xyz.junerver.fileselector

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Environment
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import xyz.junerver.fileselector.worker.ANDROID_DATA_PATH
import xyz.junerver.fileselector.worker.ActivityUIWorker
import xyz.junerver.fileselector.worker.FilesScanWorker
import java.lang.ref.SoftReference

/**
 * Description: 入口 & 配置
 * @author Junerver
 * date: 2021/12/27-9:50
 * Email: junerver@gmail.com
 * Version: v1.0
 */

const val VERSION = "0.1.17"

class FileSelector private constructor(ctx: Context) {
    //使用软引用持有ctx对象以避免内存泄漏
    private val mSrCtx: SoftReference<Context> = SoftReference(ctx)

    fun setSelectPath(vararg selects: String): FileSelector {
        selectPaths = selects as Array<String>
        if (!selectPaths.isNullOrEmpty()) {
            "~~~~~~~~~~~~~~~~~~~ 选择的文件目录 ~~~~~~~~~~~~~~~~~~~".log()
            selectPaths.forEach {
                it.log()
            }
        }
        return this
    }

    fun setIgnorePath(vararg paths: String): FileSelector {
        ignorePaths = paths as Array<String>
        if (!ignorePaths.isNullOrEmpty()) {
            "~~~~~~~~~~~~~~~~~~~ 忽略的文件目录 ~~~~~~~~~~~~~~~~~~~".log()
            ignorePaths.forEach {
                it.log()
            }
        }

        return this
    }

    fun isShowHiddenFile(show: Boolean): FileSelector {
        isShowHidden = show
        return this
    }

    fun setBarColorRes(@ColorRes barColor: Int): FileSelector {
        return setBarColorInt(ContextCompat.getColor(mSrCtx.get()!!, barColor))
    }

    fun setBarColorInt(@ColorInt color: Int): FileSelector {
        barColor = color
        return this
    }

    fun setMaxCount(max: Int): FileSelector {
        maxCount = Math.max(max, 1)
        return this
    }

    fun setFileType(vararg fileTypes: String): FileSelector {
        val types = fileTypes as Array<String>
        mFileTypes = types.map { it.lowercase() }.toTypedArray()
        return this
    }

    fun setFileTypes(fileTypes: Array<String>?): FileSelector {
        if (fileTypes != null) {
            mFileTypes = fileTypes
        }
        return this
    }

    fun setSortType(sortType: Int): FileSelector {
        mSortType = sortType
        return this
    }

    fun setDebug(debug: Boolean): FileSelector {
        isDebugLog = debug
        return this
    }

    /**
     * Description: 主动请求权限，需要在resume中处理判断是否授权
     * @author Junerver
     * @date: 2021/12/30-9:09
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
    fun requestManagerFiles() {
        //没有文件管理权限去申请
        mSrCtx.get()?.apply {
            startActivity(Intent(this,RequestPermissionsActivity::class.java))
        }
    }

    /**
     * Description: 启动内置的UI用于显示扫描结果
     * @author Junerver
     * @date: 2021/12/27-12:37
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
    fun startUIWorker(): ActivityUIWorker {
        return ActivityUIWorker(mSrCtx)
    }

    /**
     * Description: 仅按照配置扫描文件
     * @author Junerver
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
    fun startScanWorker(): FilesScanWorker {
        return FilesScanWorker(mSrCtx)
    }

    companion object {
        const val BY_NAME_ASC = 0
        const val BY_NAME_DESC = 1
        const val BY_TIME_ASC = 2
        const val BY_TIME_DESC = 3
        const val BY_SIZE_ASC = 4
        const val BY_SIZE_DESC = 5
        const val BY_EXTENSION_ASC = 6
        const val BY_EXTENSION_DESC = 7
        const val BY_DATA_ASC = 8
        const val BY_DATA_DESC = 9

        const val TARGET_DIR_PATH_QQ = "$ANDROID_DATA_PATH/com.tencent.mobileqq/Tencent/QQfile_recv"
        const val TARGET_DIR_PATH_WECHAT = "$ANDROID_DATA_PATH/com.tencent.mm/MicroMsg/Download"
        const val TARGET_DIR_PATH_THUNDER = "$ANDROID_DATA_PATH/com.xunlei.downloadprovider/files/ThunderDownload"

        val ICON_MAP = mapOf(
            TARGET_DIR_PATH_QQ to R.drawable.icon_qq,
            TARGET_DIR_PATH_WECHAT to R.drawable.icon_wechat,
            TARGET_DIR_PATH_THUNDER to R.drawable.icon_thunder
        )

        val IMAGE_TYPES: Array<String> = arrayOf(
            "bmp", "jpeg", "jpg", "png", "tif", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd", "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "WMF", "webp", "avif", "apng"
        )
        val VIDEO_TYPES: Array<String> = arrayOf()
        val DOC_TYPES: Array<String> = arrayOf()

        internal var mFileTypes: Array<String> = arrayOf()
        internal var mSortType = BY_TIME_DESC
        internal var maxCount = 9
        internal var barColor = Color.parseColor("#1bbc9b")
        internal var isShowHidden = false
        internal var selectPaths: Array<String> = arrayOf(
            "/storage/emulated/0/",
            "/storage/emulated/0/Android/data/",
        )
        internal var ignorePaths: Array<String> = arrayOf()
        private var instance: FileSelector? = null
        internal var isDebugLog = false

        fun getInstance(context: Context): FileSelector {
            "++++++++++++++++++ Versin: $VERSION ++++++++++++++++++++++".log()
            if (instance == null) {
                instance = FileSelector(context)
            }
            return instance as FileSelector
        }
    }
}