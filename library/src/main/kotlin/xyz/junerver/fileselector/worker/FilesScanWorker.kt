package xyz.junerver.fileselector.worker

import xyz.junerver.fileselector.*
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import android.content.Context
import android.net.Uri

import androidx.documentfile.provider.DocumentFile
import java.lang.ref.SoftReference
import java.net.URLDecoder


/**
 * Description: 搜索worker，实际的业务流程
 * @author Junerver
 * date: 2021/12/27-10:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */

const val ANDROID_DATA_PATH: String = "/storage/emulated/0/Android/data"

class FilesScanWorker(private val mSrCtx: SoftReference<Context>) {

    private var mCallBack: FilesScanCallBack? = null


    interface FilesScanCallBack {
        fun onNext(fileModels: List<FileModel>)
        fun onCompleted(fileModels: List<FileModel>)
    }

    fun setCallBack(callBack: FilesScanCallBack): FilesScanWorker {
        mCallBack = callBack
        return this
    }


    private fun getFiles(): List<FileModel?> {
        if (FileSelector.selectPaths.isNotEmpty()) {
            "需要遍历的文件：${FileSelector.selectPaths.joinToString()}".log()
            for (selectPath in FileSelector.selectPaths) {
                if (selectPath.contains(ANDROID_DATA_PATH)) {
                    var path = selectPath
                    if (selectPath.endsWith("/")) {
                        path = selectPath.substring(0, selectPath.lastIndex)
                        path.log()
                    }
                    DocumentFile.fromTreeUri(
                        mSrCtx.get()!!,
                        Uri.parse(FileUriUtils.changeToUri3(path))
                    )?.let {
                        GlobalThreadPools.getInstance().execute { getDataFolderFiles(it) }
                    }
                } else {
                    //非data目录普通遍历
                    GlobalThreadPools.getInstance().execute { getFolderFiles(selectPath) }
//                    GlobalThreadPools.getInstance().execute { getFolderFilesByKt(selectPath) }
                }
            }
        } else {
            "选择目录为空！".log()
        }
        try {
            Thread.sleep(100)
            while (!GlobalThreadPools.getInstance().hasDone()) {
                Thread.sleep(20)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val list = ArrayList(mFileModelSet)
        mCallBack?.run {
            postUI {
                onCompleted(list)
            }
        }
        "Done paths: ${mFilesIndexMap.size}  files:${mFileModelSet.size}  data:${mDataCount}".log()
        return list
    }

    /**
     * Description: 普通文件的遍历方式
     * @author Junerver
     * @date: 2021/12/30-16:50
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
    private fun getFolderFiles(path: String) {
        val currentDir = File(path)
        val absPath = currentDir.absolutePath
        val files = currentDir.listFiles { file ->
            if (file.isDirectory) {
                !(!FileSelector.isShowHidden && file.isHidden)
            } else {
                if (FileSelector.mFileTypes.isNotEmpty()) {
                    getExtensionByName(file.name.toString()).lowercase() in FileSelector.mFileTypes
                } else {
                    !(!FileSelector.isShowHidden && file.isHidden)
                }
            }
        }
        if (files.isNullOrEmpty()) {
            return
        }
        val dirs: MutableList<File> = ArrayList()
        val fms: MutableList<FileModel> = ArrayList()
        for (value in files) {
            if (value.isFile) {
                val pathStr = value.absolutePath
                val fileModel = FileModel(
                    pathStr,
                    getFileName(pathStr),
                    getExtension(pathStr),
                    value.length(),
                    value.lastModified()
                )
                fms.add(fileModel)
            } else {
                dirs.add(value)
            }
        }
        if (fms.isNotEmpty()) {
            mFileModelSet.addAll(fms)
            if (!mFilesIndexMap.containsKey(absPath)) {
                mFilesIndexMap[absPath] = fms
                mCallBack?.run {
                    postUI {
                        onNext(fms)
                    }
                }
            }
        }
        for (dir in dirs) {
            val dirPath = dir.absolutePath
            if (!mFilesIndexMap.containsKey(dirPath)) {
                //该路径下的文件未被遍历
                if (FileSelector.ignorePaths.isNotEmpty()) {
                    for (ignorePath in FileSelector.ignorePaths) {
                        if (!dirPath.lowercase().contains(ignorePath.lowercase())) {
                            GlobalThreadPools.getInstance().execute { getFolderFiles(dirPath) }
                        }
                    }
                } else {
                    GlobalThreadPools.getInstance().execute { getFolderFiles(dirPath) }
                }
            }
        }
    }

    /**
     * Description:3000+文件遍历 速度较慢，大概需要8s左右，而使用多线程遍历则能控制在3s左右
     * @author Junerver
     * @date: 2021/12/31-16:42
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
    private fun getFolderFilesByKt(path: String) {
        //在该目录下走一圈，得到文件目录树结构
        val fileTree: FileTreeWalk = File(path).walk()
        fileTree.maxDepth(Int.MAX_VALUE) //需遍历的目录层级为1，即无需检查子目录
            .filter { it.isFile } //只挑选文件，不处理文件夹
            .filter { it.extension in FileSelector.mFileTypes } //选择扩展名为png和jpg的图片文件
            .filter { !(!FileSelector.isShowHidden && it.isHidden) }
            .forEach {
                val fileModel = FileModel(
                    it.absolutePath,
                    getFileName(it.absolutePath),
                    getExtension(it.absolutePath),
                    it.length(),
                    it.lastModified()
                )
                mFileModelSet.add(fileModel)
                mCallBack?.run {
                    postUI {
                        onNext(arrayListOf(fileModel))
                    }
                }
            } //循环处理符合条件的文件
    }

    /**
     * Description: 遍历data
     * @author Junerver
     * @date: 2021/12/30-17:20
     * @Email: junerver@gmail.com
     * @Version: v1.0
     * @param
     * @return
     */
    private fun getDataFolderFiles(documentFile: DocumentFile) {
        if (documentFile.isDirectory) {
            "data: ${documentFile.uri}  ${documentFile.isDirectory} +${Thread.currentThread().name}".log()
            val fms: MutableList<FileModel> = ArrayList()
            for (file in documentFile.listFiles()) {
                if (file.isFile && documentFile.uri.toString() in TARGET_DIR_PATH) {
                    //文件
                    if (getExtensionByName(file.name.toString()).lowercase() in FileSelector.mFileTypes) {
                        val fileModel = FileModel(
                            URLDecoder.decode(
                                FileUriUtils.treeToPath(file.uri.toString()),
                                "UTF-8"
                            ),
                            file.name.toString(),
                            getExtensionByName(file.name.toString()),
                            file.length(),
                            file.lastModified()
                        )
                        fileModel.documentFile = file
                        fms.add(fileModel)
                    }
                } else {
                    //目录
                    val path = file.uri.toString()
                    //只遍历允许遍历的文件夹
                    if (path in INCLUDE_PACKAGE_DIR_LEVEL) {
                        INCLUDE_PACKAGE_DIR_LEVEL.remove(path)
                        GlobalThreadPools.getInstance().execute { getDataFolderFiles(file) }
                    }
                }
            }
            if (fms.isNotEmpty()) {
                mFileModelSet.addAll(fms)
                if (!mFilesIndexMap.containsKey(documentFile.uri.path)) {
                    mFilesIndexMap[documentFile.uri.path.toString()] = fms
                    mCallBack?.run {
                        postUI {
                            "data scanned：${fms.size} ".log()
                            mDataCount += fms.size
                            onNext(fms)
                        }
                    }
                }
            }
        }
    }

    /**
     * Description: 开启线程开始扫描文件
     * @author Junerver
     * @Version: v1.0
     * @param
     * @return
     */
    fun work() {
        mDataCount = 0
        mFileModelSet.clear()
        mFilesIndexMap.clear()
        Thread { getFiles() }.start()
    }

    companion object {
        private var mDataCount = 0
        val mFileModelSet = CopyOnWriteArraySet<FileModel>()
        private val mFilesIndexMap = HashMap<String, List<FileModel>>()
        val INCLUDE_PACKAGE_DIR_LEVEL = arrayListOf(
            //QQ文件目录层级
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mobileqq/Tencent/QQfile_recv"),
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mobileqq/Tencent"),
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mobileqq"),
            //微信目录层级
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mm/MicroMsg/Download"),
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mm/MicroMsg"),
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mm"),
            //迅雷目录层级
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.xunlei.downloadprovider/files/ThunderDownload"),
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.xunlei.downloadprovider/files"),
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.xunlei.downloadprovider"),
        )

        val TARGET_DIR_PATH = arrayListOf(
            //QQ文件目录层级
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mobileqq/Tencent/QQfile_recv"),
            //微信目录层级
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.tencent.mm/MicroMsg/Download"),
            //迅雷目录层级
            FileUriUtils.changeToUri2("$ANDROID_DATA_PATH/com.xunlei.downloadprovider/files/ThunderDownload"),
        )
    }

}