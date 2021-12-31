package xyz.junerver.sfbrowser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.lxj.xpopup.XPopup
import xyz.junerver.fileselector.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

/**
 * Description:
 * @author Junerver
 * date: 2021/12/31-7:44
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class BrowserItemOnClickOnClickListenerImpl(private val mCallBack: OperateFileModelItemCallBack) :
    FileAdapter.BrowserItemOnClickListener {

    override fun onItemClick(ctx: Context, fileModel: FileModel) {
        val ex = getExtension(fileModel.path).lowercase()
        if (FileSelector.IMAGE_TYPES.contains(ex)) {
            //图片格式
            val i = Intent(ctx, PictureActivity::class.java)
            i.putExtra("path", fileModel.path)
            ctx.startActivity(i)
        } else {
            if (!fileModel.isAndroidData) {
                ctx.openFile(fileModel.path)
            } else {
                ctx.toast("无法打开Android/data目录下的文件！请长按使用提取功能!")
            }
        }
    }

    override fun onItemLongClick(ctx: Context, fileModel: FileModel) {
        XPopup.Builder(ctx)
            .asBottomList(
                "", MENU_ITEMS
            ) { _, text ->
                val oldFile = File(fileModel.path)
                when (text) {
                    EXTRACT_FILE -> {
                        //复制文件到SF的目录下
                        val target = File(ROOT_DIR + fileModel.name)
                        if (target.exists() && target.isFile && target.length() > 0) {
                            ctx.toast("目标文件已存在！")
                            return@asBottomList
                        }
                        if (!fileModel.isAndroidData) {
                            if (oldFile.exists() && oldFile.isFile) {
                                Files.copy(oldFile.toPath(), target.toPath())
                                ctx.toast("文件提取成功：${target.path}")
                            } else {
                                ctx.toast("无效文件，操作失败！")
                            }
                        } else {
                            //
                            GlobalThreadPools.getInstance().execute {
                                fileModel.documentFile?.let { doc ->
                                    val fis = ctx.contentResolver.openInputStream(doc.uri)
                                    fis?.let { `is` ->
                                        val bis = BufferedInputStream(`is`)
                                        val fos = FileOutputStream(target)
                                        try {
                                            val buffer = ByteArray(1024)
                                            var len: Int
                                            var total = 0
                                            while (((bis.read(buffer)).also { len = it }) != -1) {
                                                fos.write(buffer, 0, len)
                                                total += len
                                                //获取当前下载量
                                                val progress: Double =
                                                    total.toDouble() / fileModel.size
                                                "下载量： $progress".log()
                                            }
                                            postUI {
                                                ctx.toast("文件提取成功：${target.path}")
                                            }
                                        } catch (e: Exception) {
                                            postUI {
                                                ctx.toast("文件提取失败！")
                                            }
                                        } finally {
                                            fos.close()
                                            bis.close()
                                            `is`.close()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    DELETE -> {
                        fun showDelDialog(delAct: () -> Unit) {
                            AlertDialog.Builder(ctx)
                                .setTitle("警告：危险操作不可逆！")
                                .setNeutralButton("取消") { _, _ -> }
                                .setPositiveButton("确定") { _, _ ->
                                    delAct.invoke()
                                    mCallBack.delItem(fileModel)
                                    ctx.toast("删除成功！")
                                }.show()
                        }
                        if (!fileModel.isAndroidData && oldFile.exists() && oldFile.isFile) {
                            showDelDialog { oldFile.delete() }
                        } else if (fileModel.isAndroidData && fileModel.documentFile != null) {
                            showDelDialog { fileModel.documentFile!!.delete() }
                        } else {
                            ctx.toast("无效文件，操作失败！")
                        }
                    }
                    COPY_PATH -> {
                        //获取剪贴板管理器：
                        val cm: ClipboardManager =
                            ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        // 创建普通字符型ClipData
                        val mClipData = ClipData.newPlainText("SFbrowser", fileModel.path)
                        // 将ClipData内容放到系统剪贴板里。
                        cm.setPrimaryClip(mClipData)
                        ctx.toast("已复制路径：${fileModel.path}")
                    }
                    SEE_DETAIL -> {
                        XPopup.Builder(ctx)
                            .asCustom(FileDetailsPopup(ctx, fileModel))
                            .show()
                    }
                    RENAME -> {
                        fun showRenamePopup(rename: (newName: String) -> Unit) {
                            XPopup.Builder(ctx).asInputConfirm(
                                "重命名", "", getNameExcludeExtension(fileModel.path), null
                            ) { text ->
                                val newName = text + ".${fileModel.extension}"
                                if (newName.isValidFileName()) {
                                    rename(newName)
                                    mCallBack.changeItem(fileModel)
                                } else {
                                    ctx.toast("文件名非法！")
                                }
                            }.show()
                        }
                        if (oldFile.exists() && oldFile.isFile && !fileModel.isAndroidData) {
                            showRenamePopup { newName ->
                                val target = File(oldFile.parent + File.separator + newName)
                                oldFile.renameTo(target)
                                fileModel.update(target)
                            }
                        } else if (fileModel.isAndroidData && fileModel.documentFile != null) {
                            showRenamePopup {
                                fileModel.documentFile!!.renameTo(it)
                                fileModel.name = it
                            }
                        } else {
                            ctx.toast("无效文件，操作失败！")
                        }
                    }
                }
            }
            .show()
    }

    companion object {
        private const val EXTRACT_FILE = "提取"
        private const val DELETE = "删除"
        private const val COPY_PATH = "复制路径"
        private const val SEE_DETAIL = "查看详情"
        private const val RENAME = "重命名"
        private val MENU_ITEMS = arrayOf(EXTRACT_FILE, DELETE, COPY_PATH, SEE_DETAIL, RENAME)
    }
}

