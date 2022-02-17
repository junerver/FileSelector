package xyz.junerver.fileselector


import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * 文件处理
 *
 * @author Lee
 */

fun getDateTime(millis: Long): String {
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

fun getNameExcludeExtension(path: String): String {
    return try {
        var fileName = getFileName(path)
        val lastIndexOf = fileName.lastIndexOf(".")
        if (lastIndexOf != -1) {
            fileName = fileName.substring(0, lastIndexOf)
        }
        fileName
    } catch (e: Exception) {
        ""
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

fun formatFileSize(size: Long): String {
    val df = DecimalFormat("#.00")
    val fileSizeString: String = when {
        size < 1024 -> {
            df.format(size.toDouble()) + "B"
        }
        size < 1048576 -> {
            df.format(size.toDouble() / 1024) + "K"
        }
        size < 1073741824 -> {
            df.format(size.toDouble() / 1048576) + "M"
        }
        else -> {
            df.format(size.toDouble() / 1073741824) + "G"
        }
    }
    return fileSizeString
}

fun getFileParentPath(path: String): String {
    return path.substring(0,path.lastIndexOf("/"))
}
