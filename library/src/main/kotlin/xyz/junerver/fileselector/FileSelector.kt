package xyz.junerver.fileselector

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
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
class FileSelector private constructor(ctx: Context) {
    //使用软引用持有ctx对象以避免内存泄漏
    private val mSrCtx: SoftReference<Context> = SoftReference(ctx)

    init {
        selectPaths = arrayOf(
            "/storage/emulated/0/DCIM",
            "/storage/emulated/0/Android/data/" + mSrCtx.get()?.packageName + "/"
        )
    }

    fun setSelectPath(vararg selects: String): FileSelector {
        selectPaths = selects as Array<String>
        return this
    }

    fun setIgnorePath(vararg paths: String): FileSelector {
        ignorePaths = paths as Array<String>
        return this
    }

    fun isShowHiddenFile(show: Boolean): FileSelector {
        isShow = show
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
        mFileTypes = fileTypes as Array<String>
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
        return FilesScanWorker
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

        internal var mFileTypes: Array<String> = arrayOf()
        internal var mSortType = BY_NAME_ASC
        internal var maxCount = 9
        internal var barColor = Color.parseColor("#1bbc9b")
        internal var isShow = false
        internal var selectPaths: Array<String> = arrayOf()
        internal var ignorePaths: Array<String> = arrayOf()
        private var instance: FileSelector? = null

        fun getInstance(context: Context): FileSelector {
            if (instance == null) {
                instance = FileSelector(context)
            }
            return instance as FileSelector
        }
    }
}