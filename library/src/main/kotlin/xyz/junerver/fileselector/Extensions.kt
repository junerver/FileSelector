package xyz.junerver.fileselector

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


fun AppCompatActivity.toast(msg: String) = Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()

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

fun String.log() = Log.d("FileSelector", this)

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

object KitUtil{
    val handler: Handler by lazy {  Handler(Looper.getMainLooper()) }
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

