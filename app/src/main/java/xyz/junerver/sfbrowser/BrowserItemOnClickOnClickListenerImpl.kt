package xyz.junerver.sfbrowser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.lxj.xpopup.XPopup
import xyz.junerver.fileselector.*
import java.io.File
import java.nio.file.Files

/**
 * Description:
 * @author Junerver
 * date: 2021/12/31-7:44
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class BrowserItemOnClickOnClickListenerImpl(private val mCallBack: OperateFileModelItemCallBack) : FileAdapter.BrowserItemOnClickListener {
    override fun onItemClick(ctx: Context, fileModel: FileModel) {
        val ex = getExtension(fileModel.path).lowercase()
        if (FileSelector.IMAGE_TYPES.contains(ex)) {
            //图片格式
            val i = Intent(ctx, PictureActivity::class.java)
            i.putExtra("path", fileModel.path)
            ctx.startActivity(i)
        } else {
            ctx.openFile(fileModel.path)
        }
    }

    override fun onItemLongClick(ctx: Context, fileModel: FileModel) {
        //TODO("弹出操作对话框：提取、删除、复制路径、查看详情、重命名")
        XPopup.Builder(ctx)
            .asBottomList(
                "", arrayOf("提取", "删除", "复制路径", "查看详情", "重命名")
            ) { position, _ ->
                val oldFile = File(fileModel.path)
                when (position) {
                    0 -> {
                        //复制文件到SF的目录下
                        val target = File(ROOT_DIR + fileModel.name)
                        if (oldFile.exists() && oldFile.isFile) {
                            Files.copy(oldFile.toPath(), target.toPath())
                            ctx.toast("文件提取成功：${target.path}")
                        } else {
                            ctx.toast("无效文件，操作失败！")
                        }
                    }
                    1 -> {
                        if (oldFile.exists() && oldFile.isFile) {
                            AlertDialog.Builder(ctx)
                                .setTitle("警告：危险操作不可逆！")
                                .setNeutralButton("取消") { _, _ -> }
                                .setPositiveButton("确定") { _, _ ->
                                    oldFile.delete()
                                    mCallBack.delItem(fileModel)
                                    ctx.toast("删除成功！")
                                }.show()
                        } else {
                            ctx.toast("无效文件，操作失败！")
                        }
                    }
                    2 -> {
                        //获取剪贴板管理器：
                        val cm: ClipboardManager =
                            ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        // 创建普通字符型ClipData
                        val mClipData = ClipData.newPlainText("SFbrowser", fileModel.path)
                        // 将ClipData内容放到系统剪贴板里。
                        cm.setPrimaryClip(mClipData)
                        ctx.toast("已复制路径：${fileModel.path}")
                    }
                    3 -> {
                        XPopup.Builder(ctx)
                            .asCustom(FileDetailsPopup(ctx, fileModel))
                            .show()
                    }
                    4 -> {
                        if (oldFile.exists() && oldFile.isFile) {
                            XPopup.Builder(ctx).asInputConfirm(
                                "重命名", "", getNameExcludeExtension(fileModel.path), null
                            ) { text ->
                                val newName = text + ".${fileModel.extension}"
                                if (newName.isValidFileName()) {
                                    val target =
                                        File(oldFile.parent + File.separator + newName)
                                    oldFile.renameTo(target)
                                    fileModel.update(target)
                                    mCallBack.changeItem(fileModel)
                                } else {
                                    ctx.toast("文件名非法！")
                                }

                            }.show()
                        } else {
                            ctx.toast("无效文件，操作失败！")
                        }
                    }
                }
            }
            .show()
    }
}

