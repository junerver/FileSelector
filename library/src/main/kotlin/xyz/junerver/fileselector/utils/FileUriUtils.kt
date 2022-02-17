package xyz.junerver.fileselector.utils

import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import android.net.Uri
import java.lang.StringBuilder
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract

/**
 * Description:
 *
 * @author Junerver
 * date: 2021/12/30-16:09
 * Email: junerver@gmail.com
 * Version: v1.0
 */
object FileUriUtils {
    private val root = Environment.getExternalStorageDirectory().path + "/"

    //将uri转换成真实路径
    fun treeToPath(path: String): String {
        var path2: String
        if (path.contains("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary")) {
            path2 = path.replace(
                "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A",
                root
            )
            path2 = path2.replace("%2F", "/")
        } else {
//            path2 = root + TextUtils.getSubString(path + "测试", "document/primary%3A", "测试").replace("%2F", "/");
            path2 = root
        }
        return path2
    }

    //判断是否已经获取了Data权限，改改逻辑就能判断其他目录，懂得都懂
    fun isGrant(context: Context): Boolean {
        for (persistedUriPermission in context.contentResolver.persistedUriPermissions) {
            if (persistedUriPermission.isReadPermission && persistedUriPermission.uri.toString() == "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata") {
                return true
            }
        }
        return false
    }

    //直接返回DocumentFile
    fun getDocumentFilePath(context: Context?, path: String, sdCardUri: String?): DocumentFile? {
        var document = DocumentFile.fromTreeUri(context!!, Uri.parse(sdCardUri))
        val parts = path.split("/").toTypedArray()
        for (i in 3 until parts.size) {
            document = document!!.findFile(parts[i])
        }
        return document
    }

    //转换至uriTree的路径
    fun changeToUri(path: String): String {
        var path = path
        if (path.endsWith("/")) {
            path = path.substring(0, path.length - 1)
        }
        val path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F")
        return "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A$path2"
    }

    /**
     * 通过已知的路径获取DocumentFile对象，但是该对象不是UriTree对象不能使用listFile函数 否则会抛出异常
     * 这是谷歌对没有授权的子文件夹目录进行了限制，
     * 不让你直接通过TreeUri生成正确的Document对象，
     * 至少在Android/data目录是这样的。
     * 直接通过DocumentFile.fromTreeUri函数无论怎么调用，最终结果都是一样
     * 即便生成的是Android/data目录下子文件的正确URI，再生成DocumentFile对象，
     * 还是不行，因为你生成的DocumentFile对象始终指向Android/data(也就是你授权过的那个目录),
     * 此题无解！
     * @param context
     * @param path
     * @return
     */
    fun getDocumentFile(context: Context?, path: String): DocumentFile? {
        var path = path
        if (path.endsWith("/")) {
            path = path.substring(0, path.length - 1)
        }
        val path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F")
        return DocumentFile.fromSingleUri(
            context!!,
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A$path2")
        )
    }

    /**
     * 转换至uriTree的路径
     * ** 注意： 此方法只能用来转换授权目录下的子路径 将它变换成便利时对照的Uri **
     * @param path
     * @return
     */
    fun changeToUri2(path: String): String {
        val paths =
            path.replace("/storage/emulated/0/Android/data", "").split("/").toTypedArray()
        val stringBuilder =
            StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata")
        for (p in paths) {
            if (p.isEmpty()) continue
            stringBuilder.append("%2F").append(p)
        }
        return stringBuilder.toString()
    }

    /**
     * 转换至uriTree的路径
     * ** 注意： 此方法只能用来转换已授权的目录路径 **
     * @param path
     * @return
     */
    fun changeToUri3(path: String): String {
        var path = path
        path = path.replace("/storage/emulated/0/", "").replace("/", "%2F")
        return "content://com.android.externalstorage.documents/tree/primary%3A$path"
    }

    //获取指定目录的权限
    fun startFor(path: String, context: Activity, REQUEST_CODE_FOR_DIR: Int) {
//        statusHolder.path = path;
        val uri = changeToUri(path)
        val parse = Uri.parse(uri)
        val intent = Intent("android.intent.action.OPEN_DOCUMENT_TREE")
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, parse)
        }
        context.startActivityForResult(intent, REQUEST_CODE_FOR_DIR)
    }

    //直接获取data权限，推荐使用这种方案
    fun startForRoot(context: Activity, REQUEST_CODE_FOR_DIR: Int) {
        val uri1 =
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
        var uri = changeToUri(Environment.getExternalStorageDirectory().path)
        uri = uri + "/document/primary%3A" + Environment.getExternalStorageDirectory().path.replace(
            "/storage/emulated/0/",
            ""
        ).replace("/", "%2F")
        val parse = Uri.parse(uri)
        val documentFile = DocumentFile.fromTreeUri(context, uri1)
        val intent1 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent1.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile!!.uri)
        context.startActivityForResult(intent1, REQUEST_CODE_FOR_DIR)
    }
}