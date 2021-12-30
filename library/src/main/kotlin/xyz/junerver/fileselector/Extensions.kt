package xyz.junerver.fileselector

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.util.regex.Pattern


fun AppCompatActivity.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun View.gone() {
    this.visibility = View.GONE
}

fun gones(vararg views: View) {
    views.forEach {
        it.gone()
    }
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun invisibles(vararg views: View) {
    views.forEach {
        it.invisible()
    }
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun visibles(vararg views: View) {
    views.forEach {
        it.visible()
    }
}

fun String.log() {
    if (FileSelector.isDebugLog) {
        Log.d("FileSelector", this)
    }
}

fun <T> T.postUI(action: () -> Unit) {

    // Fragment
    if (this is Fragment) {
        val fragment = this
        if (!fragment.isAdded) return

        val activity = fragment.activity ?: return
        if (activity.isFinishing) return

        activity.runOnUiThread(action)
        return
    }

    // Activity
    if (this is Activity) {
        if (this.isFinishing) return

        this.runOnUiThread(action)
        return
    }

    // 主线程
    if (Looper.getMainLooper() === Looper.myLooper()) {
        action()
        return
    }

    // 子线程，使用handler
    KitUtil.handler.post { action() }
}

object KitUtil {
    val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
}

//region DSL实现的监听器
fun TextView.addTextChangedListenerDsl(init: TextChangedListenerDsl.() -> Unit) {
    val listener = TextChangedListenerDsl()
    listener.init()
    this.addTextChangedListener(listener)
}

class TextChangedListenerDsl : TextWatcher {

    private var afterTextChanged: ((Editable?) -> Unit)? = null

    private var beforeTextChanged: ((CharSequence?, Int, Int, Int) -> Unit)? = null

    private var onTextChanged: ((CharSequence?, Int, Int, Int) -> Unit)? = null

    /**
     * DSL方法
     */
    fun afterTextChanged(method: (Editable?) -> Unit) {
        afterTextChanged = method
    }

    fun beforeTextChanged(method: (CharSequence?, Int, Int, Int) -> Unit) {
        beforeTextChanged = method
    }

    fun onTextChanged(method: (CharSequence?, Int, Int, Int) -> Unit) {
        onTextChanged = method
    }

    /**
     * 原始方法
     */
    override fun afterTextChanged(s: Editable?) {
        afterTextChanged?.invoke(s)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        beforeTextChanged?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged?.invoke(s, start, before, count)
    }

}
//endregion

fun Context.showManagerFileTips(cancel: () -> Unit = {}, request: (intent: Intent) -> Unit = {}) {
    AlertDialog.Builder(this)
        .setTitle("提示：")
        .setCancelable(false)
        .setMessage("请授予应用文件访问权限，否则会导致部分文件无法显示！")
        .setNeutralButton("取消") { _, _ -> cancel.invoke() }
        .setPositiveButton("去授权") { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:" + this.packageName)
            request(intent)
        }
        .show()
}

fun Context.showRequestDataTips(cancel: () -> Unit = {}, request: () -> Unit = {}) {
    AlertDialog.Builder(this)
        .setTitle("提示：")
        .setCancelable(false)
        .setMessage("请在下一个页面授予应用data目录访问权限，否则会导致部分文件无法显示！")
        .setNeutralButton("取消") { _, _ -> cancel.invoke() }
        .setPositiveButton("去授权") { _, _ ->
            request.invoke()
        }
        .show()
}

fun Context.getUriForFile(file: File): Uri {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            this.applicationContext,
            "${this.packageName}.fileprovider",
            file)
    } else {
        Uri.fromFile(file)
    }
}

fun Context.openFile(file:String){
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(this.getUriForFile(File(file)), MapTable.getMIMEType(file))
        this.startActivity(intent)
        Intent.createChooser(intent, "请选择对应的软件打开该附件！")
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show()
    }
}

fun Context.shareFile(file:String){
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(this.getUriForFile(File(file)), MapTable.getMIMEType(file))
        this.startActivity(intent)
        Intent.createChooser(intent, "请选择对应的软件打开该附件！")
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show()
    }
}

fun String.isValidFileName(): Boolean {
    val regex = "[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$"
    return Pattern.matches(regex,this)
}