package xyz.junerver.fileselector.worker

import xyz.junerver.fileselector.*
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Description: 搜索worker，实际的业务流程
 * @author Junerver
 * date: 2021/12/27-10:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */
object FilesScanWorker {

    private var mCallBack: FilesScanCallBack? = null
    val mFileModelSet = CopyOnWriteArraySet<FileModel>()
    private val mFilesIndexMap = HashMap<String, List<FileModel>>()

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
            for (selectPath in FileSelector.selectPaths) {
                GlobalThreadPools.getInstance().execute { getFolderFiles(selectPath) }
            }
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
        "Done paths: ${mFilesIndexMap.size}  files:${mFileModelSet.size}".log()
        return list
    }

    private fun getFolderFiles(path: String) {
        val currentDir = File(path)
        val absPath = currentDir.absolutePath
        val files = currentDir.listFiles { file ->
            if (file.isDirectory) {
                !(!FileSelector.isShowHidden && file.isHidden)
            } else {
                if (FileSelector.mFileTypes.isNotEmpty()) {
                    FileSelector.mFileTypes.find {
                        it.lowercase() == getExtensionByName(file.name).lowercase()
                    } != null
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
            if (!mFilesIndexMap.keys.contains(absPath)) {
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
            if (!mFilesIndexMap.keys.contains(dirPath)) {
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
     * Description: 开启线程开始扫描文件
     * @author Junerver
     * @Version: v1.0
     * @param
     * @return
     */
    fun work() {
        mFileModelSet.clear()
        mFilesIndexMap.clear()
        Thread { getFiles() }.start()
    }

}