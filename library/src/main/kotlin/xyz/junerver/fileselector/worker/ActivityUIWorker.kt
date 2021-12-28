package xyz.junerver.fileselector.worker

import android.content.Context
import android.content.Intent
import xyz.junerver.fileselector.FileModel
import xyz.junerver.fileselector.FileSelectorActivity
import xyz.junerver.fileselector.OnResultListener
import java.lang.ref.SoftReference

/**
 * Description:
 * @author Junerver
 * date: 2021/12/27-11:14
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class ActivityUIWorker(private val mSrCtx: SoftReference<Context>) {


    fun forResult(lis: OnResultListener<FileModel>): ActivityUIWorker {
        listener = lis
        return this
    }

    fun start() {
        val intent = Intent(mSrCtx.get(), FileSelectorActivity::class.java)
        mSrCtx.get()?.startActivity(intent)
    }

    companion object{
        var listener: OnResultListener<FileModel>? = null
    }
}