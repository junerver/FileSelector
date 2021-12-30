package xyz.junerver.sfbrowser

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import xyz.junerver.fileselector.*
import java.io.File
import kotlin.reflect.KClass

import com.lxj.xpopup.XPopup
import java.nio.file.Files
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


class MainActivity : FileSelectorActivity() {

    init {
        FileSelector.getInstance(this)
            .setFileType(
                "jpg",
                "gif",
                "png",
                "bmp",
                "jpeg",
                "webp",
                "wmv",
                "flv",
                "mp4",
                "avi",
                "mpg",
                "mpeg",
                "rmvb",
                "rm",
                "asf",
                "f4v",
                "vob",
                "mkv",
                "3gp",
                "mov",
                "mid",
                "wav",
                "wma",
                "mp3",
                "ogg",
                "amr",
                "m4a",
                "3gpp",
                "aac",
                "swf",
                "wps",
                "doc",
                "docx",
                "txt",
                "xlsx",
                "xls",
                "pdf",
                "ppt",
                "pptx",
                "zip",
                "rar",
                "7z",
                "exe",
                "gsp",
                "bbx",
            )

        if (!File(ROOT_DIR).exists()) {
            File(ROOT_DIR).mkdirs()
        }
    }

    override var isSelectorMode = false

    override fun initToolBar(title: String) {
        super.initToolBar("文件浏览")
        mToolBar.navigationIcon = null
        mToolBar.setNavigationOnClickListener { }
    }

    override fun initAdapter() {
        super.initAdapter()
        mFileAdapter.setListener(object : FileAdapter.BrowserItemListener {
            override fun onItemClick(path: String) {
                val ex = getExtension(path).lowercase()
                if (FileSelector.IMAGE_TYPES.contains(ex)) {
                    //图片格式
                    val i = Intent(mContext, PictureActivity::class.java)
                    i.putExtra("path", path)
                    startActivity(i)
                } else {
                    openFile(path)
                }
            }

            override fun onItemLongClick(path: String, fileModel: FileModel) {
                //TODO("弹出操作对话框：提取、删除、复制路径、查看详情、重命名")
                XPopup.Builder(mContext)
                    .asBottomList(
                        "", arrayOf("提取", "删除", "复制路径", "查看详情", "重命名")
                    ) { position, _ ->
                        val oldFile = File(path)
                        when (position) {
                            0 -> {
                                //复制文件到SF的目录下
                                val target = File(ROOT_DIR + fileModel.name)
                                if (oldFile.exists() && oldFile.isFile) {
                                    Files.copy(oldFile.toPath(), target.toPath())
                                    toast("文件提取成功：${target.path}")
                                } else {
                                    toast("无效文件，操作失败！")
                                }
                            }
                            1 -> {
                                if (oldFile.exists() && oldFile.isFile) {
                                    AlertDialog.Builder(mContext)
                                        .setTitle("警告：危险操作不可逆！")
                                        .setNeutralButton("取消") { _, _ -> }
                                        .setPositiveButton("确定") { _, _ ->
                                            oldFile.delete()
                                            delItem(fileModel)
                                            toast("删除成功！")
                                        }.show()
                                } else {
                                    toast("无效文件，操作失败！")
                                }
                            }
                            2 -> {
                                //获取剪贴板管理器：
                                val cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                // 创建普通字符型ClipData
                                val mClipData = ClipData.newPlainText("SFbrowser", path)
                                // 将ClipData内容放到系统剪贴板里。
                                cm.setPrimaryClip(mClipData)
                                toast("已复制路径：$path")
                            }
                            3 -> {
                                XPopup.Builder(mContext)
                                    .asCustom(FileDetailsPopup(mContext,fileModel))
                                    .show()
                            }
                            4 -> {
                                if (oldFile.exists() && oldFile.isFile) {
                                    XPopup.Builder(mContext).asInputConfirm(
                                        "重命名", ""
                                    ) { text ->
                                        val newName = text+".${fileModel.extension}"
                                        if (newName.isValidFileName()) {
                                            val target =
                                                File(oldFile.parent + File.separator + newName)
                                            oldFile.renameTo(target)
                                            fileModel.update(target)
                                            changeItem(fileModel)
                                        } else {
                                            toast("文件名非法！")
                                        }

                                    }.show()
                                } else {
                                    toast("无效文件，操作失败！")
                                }
                            }
                        }
                    }
                    .show()
            }
        })
    }

    override fun openSearchUI(clazz: KClass<out FileSearchActivity>) {
        super.openSearchUI(OpenSearchActivity::class)
    }
}