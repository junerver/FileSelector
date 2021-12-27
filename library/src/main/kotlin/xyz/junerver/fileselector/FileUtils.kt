package xyz.junerver.fileselector


import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件处理
 *
 * @author Lee
 */
object FileUtils {
    const val RESULT_KEY = "extra_result"

    @JvmStatic
    fun getDateTime(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.PRC)
        return sdf.format(cal.time)
    }

    /**
     * 获取文件名
     */
    fun getNameExcludeExtension(path: String?): String {
        return try {
            var fileName = File(path).name
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
     * 获取文件名
     */
    @JvmStatic
    fun getName(path: String): String {
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
    @JvmStatic
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
    @JvmStatic
    fun getExtension(pathOrUrl: String): String {
        val name = pathOrUrl.substring(pathOrUrl.lastIndexOf("/"))
        val dotPos = name.lastIndexOf('.')
        return if (dotPos >= 0) {
            name.substring(dotPos + 1)
        } else {
            "?"
        }
    }


}