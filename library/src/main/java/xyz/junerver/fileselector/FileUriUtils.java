package xyz.junerver.fileselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import java.io.FileFilter;

/**
 * Description:
 *
 * @author Junerver
 * date: 2021/12/30-16:09
 * Email: junerver@gmail.com
 * Version: v1.0
 */
public class FileUriUtils {
    public static String root = Environment.getExternalStorageDirectory().getPath() + "/";

    //将uri转换成真实路径
    public static String treeToPath(String path) {
        String path2;
        if (path.contains("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary")) {
            path2 = path.replace("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A", root);
            path2 = path2.replace("%2F", "/");
        } else {
//            path2 = root + TextUtils.getSubString(path + "测试", "document/primary%3A", "测试").replace("%2F", "/");
            path2 = root;
        }
        return path2;
    }


    //判断是否已经获取了Data权限，改改逻辑就能判断其他目录，懂得都懂
    public static boolean isGrant(Context context) {
        for (UriPermission persistedUriPermission : context.getContentResolver().getPersistedUriPermissions()) {
            if (persistedUriPermission.isReadPermission() && persistedUriPermission.getUri().toString().equals("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")) {
                return true;
            }
        }
        return false;
    }

    //直接返回DocumentFile
    public static DocumentFile getDocumentFilePath(Context context, String path, String sdCardUri) {
        DocumentFile document = DocumentFile.fromTreeUri(context, Uri.parse(sdCardUri));
        String[] parts = path.split("/");
        for (int i = 3; i < parts.length; i++) {
            document = document.findFile(parts[i]);
        }
        return document;
    }

    //转换至uriTree的路径
    public static String changeToUri(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" + path2;
    }

    /**
     * 通过已知的路径获取DoucmentFile对象，但是该对象不是UriTree对象不能使用listFile函数 否则会抛出异常
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
    public static DocumentFile getDoucmentFile(Context context, String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return DocumentFile.fromSingleUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" + path2));
    }


    /**
     * 转换至uriTree的路径
     * ** 注意： 此方法只能用来转换授权目录下的子路径 将它变换成便利时对照的Uri **
     * @param path
     * @return
     */
    public static String changeToUri2(String path) {
        String[] paths = path.replaceAll("/storage/emulated/0/Android/data", "").split("/");
        StringBuilder stringBuilder = new StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata");
        for (String p : paths) {
            if (p.length() == 0) continue;
            stringBuilder.append("%2F").append(p);
        }
        return stringBuilder.toString();
    }


    /**
     * 转换至uriTree的路径
     * ** 注意： 此方法只能用来转换已授权的目录路径 **
     * @param path
     * @return
     */
    public static String changeToUri3(String path) {
        path = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return ("content://com.android.externalstorage.documents/tree/primary%3A" + path);

    }

    //获取指定目录的权限
    public static void startFor(String path, Activity context, int REQUEST_CODE_FOR_DIR) {
//        statusHolder.path = path;
        String uri = changeToUri(path);
        Uri parse = Uri.parse(uri);
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, parse);
        }
        context.startActivityForResult(intent, REQUEST_CODE_FOR_DIR);

    }

    //直接获取data权限，推荐使用这种方案
    public static void startForRoot(Activity context, int REQUEST_CODE_FOR_DIR) {
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
        String uri = changeToUri(Environment.getExternalStorageDirectory().getPath());
        uri = uri + "/document/primary%3A" + Environment.getExternalStorageDirectory().getPath().replace("/storage/emulated/0/", "").replace("/", "%2F");
        Uri parse = Uri.parse(uri);
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri1);
        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
        context.startActivityForResult(intent1, REQUEST_CODE_FOR_DIR);
    }

}

