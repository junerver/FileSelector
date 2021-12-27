package xyz.junerver.fileselector.logicwork

import xyz.junerver.fileselector.FileModel
import xyz.junerver.fileselector.FileSelector
import xyz.junerver.fileselector.FileUtils.getExtension
import xyz.junerver.fileselector.FileUtils.getName
import xyz.junerver.fileselector.GlobalThreadPools
import xyz.junerver.fileselector.MyFileFilter
import java.io.File
import java.io.FileFilter
import java.util.ArrayList
import java.util.HashMap
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
    var mFileModelSet = CopyOnWriteArraySet<FileModel>()

    //对应路径下的文件 index 为路径
    private val mFilesIndexMap = HashMap<String, List<FileModel>>()

    interface FilesScanCallBack {
        fun onNext(fileModels: List<FileModel>)
        fun onCompleted(fileModels: List<FileModel>)
    }

    fun setCallBack(callBack: FilesScanCallBack): FilesScanWorker {
        mCallBack = callBack
        return this
    }

    private var mFileFilter: FileFilter? = null

    private fun getFiles(): List<FileModel?>? {
        if (mFileFilter == null) {
            mFileFilter = MyFileFilter(FileSelector.mFileTypes, FileSelector.isShow)
        }
        if (FileSelector.selectPaths != null && FileSelector.selectPaths.isNotEmpty()) {
            for (selectPath in FileSelector.selectPaths) {
                //线程池开启任务
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
        val list= ArrayList(mFileModelSet)
        if (mCallBack != null) {
            mCallBack!!.onCompleted(list)
        }
        return list
    }

    private fun getFolderFiles(path: String) {
            val file = File(path)
            val absPath = file.absolutePath
            val files = file.listFiles(mFileFilter)
            if (files == null || files.isEmpty()) {
                return
            }
            val dirs: MutableList<File> = ArrayList()
            val fms: MutableList<FileModel> = ArrayList()
            for (value in files) {
                if (value.isFile) {
                    val pathStr = value.absolutePath
                    val fileModel = FileModel(
                        pathStr,
                        getName(pathStr),
                        getExtension(pathStr),
                        value.length(),
                        value.lastModified()
                    )
                    fms.add(fileModel)
                } else {
                    //加入目录
                    dirs.add(value)
                }
            }
            //加入集合
            mFileModelSet.addAll(fms)
            if (!mFilesIndexMap.keys.contains(absPath)) {
                mFilesIndexMap[absPath] = fms
                //此处加入回调函数，从回调中获取每次的增量，通知UI更新
                if (mCallBack != null) {
                    mCallBack!!.onNext(fms)
                }
            }
            //遍历其余目录
            for (dir in dirs) {
                val dirPath = dir!!.absolutePath
                if (!mFilesIndexMap.keys.contains(dirPath)) {
                    //该路径下的文件未被遍历
                    if (FileSelector.ignorePaths != null && FileSelector.ignorePaths.isNotEmpty()) {
                        for (ignorePath in FileSelector.ignorePaths) {
                            if (!dirPath.toLowerCase().contains(ignorePath.toLowerCase())) {
                                //线程池开启任务
                                GlobalThreadPools.getInstance().execute { getFolderFiles(dirPath) }
                            }
                        }
                    } else {
                        //线程池开启任务
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