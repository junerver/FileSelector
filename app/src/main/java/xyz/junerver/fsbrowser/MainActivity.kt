package xyz.junerver.fsbrowser

import android.content.Intent
import xyz.junerver.fileselector.*
import kotlin.reflect.KClass

class MainActivity : FileSelectorActivity() {

    init {
        FileSelector.getInstance(this)
            .setFileType(
                "jpg", "gif", "png", "bmp", "jpeg", "webp", "wmv", "flv", "mp4", "avi", "mpg", "mpeg", "rmvb", "rm", "asf",
                "f4v", "vob", "mkv", "3gp", "mov", "mid", "wav", "wma", "mp3", "ogg", "amr", "m4a", "3gpp", "aac", "swf",
                "wps", "doc", "docx", "txt", "xlsx", "xls", "pdf", "ppt", "pptx", "zip", "rar", "7z", "exe", "gsp", "bbx",
                )
    }

    override var isSelectorMode = false

    override fun initToolBar(title: String) {
       super.initToolBar("文件浏览")
        mToolBar.navigationIcon = null
        mToolBar.setNavigationOnClickListener { }
    }

    override fun initAdapter() {
        super.initAdapter()
        mFileAdapter.setListener(object :FileAdapter.BrowserItemListener{
            override fun onItemClick(path: String) {
                val ex = getExtension(path).lowercase()
                if (FileSelector.IMAGE_TYPES.contains(ex)) {
                    //图片格式
                    val i = Intent(mContext, PictureActivity::class.java)
                    i.putExtra("path",path)
                    startActivity(i)
                } else {
                    openFile(path)
                }
            }

            override fun onItemLongClick(path: String) {
                TODO("弹出操作对话框：提取、删除、复制路径、查看详情、重命名")
            }
        })
    }

    override fun openSearchUI(clazz: KClass<out FileSearchActivity>) {
        super.openSearchUI(OpenSearchActivity::class)
    }
}