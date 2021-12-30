package xyz.junerver.fileselector


import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

import android.content.ActivityNotFoundException
import android.content.Context

import android.content.Intent
import android.net.Uri




/**
 * 文件处理
 *
 * @author Lee
 */

internal fun getDateTime(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC)
    return sdf.format(cal.time)
}


/**
 * 获取文件名
 */
internal fun getFileName(path: String): String {
    val dotPos = path.lastIndexOf(File.separator)
    return if (dotPos >= 0) {
        path.substring(dotPos + 1)
    } else {
        "?"
    }
}

/**
 * 获取文件扩展名
 */
fun getExtensionByName(name: String): String {
    val dotPos = name.lastIndexOf('.')
    return if (dotPos >= 0) {
        name.substring(dotPos + 1)
    } else {
        "?"
    }
}

/**
 * 获取文件扩展名
 */
fun getExtension(pathOrUrl: String): String {
    val name = pathOrUrl.substring(pathOrUrl.lastIndexOf("/"))
    val dotPos = name.lastIndexOf('.')
    return if (dotPos >= 0) {
        name.substring(dotPos + 1)
    } else {
        "?"
    }
}
