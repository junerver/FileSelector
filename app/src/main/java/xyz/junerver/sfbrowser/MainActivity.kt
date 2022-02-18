package xyz.junerver.sfbrowser

import xyz.junerver.fileselector.*
import java.io.File
import kotlin.reflect.KClass


class MainActivity : FileSelectorActivity() {

    init {
        FileSelector.getInstance(this)
            .setFileType(
                "jpg", "gif", "png", "bmp", "jpeg", "webp", "wmv", "flv", "mp4", "avi",
                "mpg", "mpeg", "rmvb", "rm", "asf", "f4v", "vob", "mkv", "3gp", "mov",
                "mid", "wav", "wma", "mp3", "ogg", "amr", "m4a", "3gpp", "aac", "swf",
                "wps", "doc", "docx", "txt", "xlsx", "xls", "pdf", "ppt", "pptx", "zip",
                "rar", "7z", "exe", "gsp", "bbx","apk",
            )
            .setDebug(BuildConfig.DEBUG)

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
        mFileAdapter.setListener(BrowserItemOnClickListenerImpl(this))
    }

    override fun openSearchUI(clazz: KClass<out FileSearchActivity>) {
        super.openSearchUI(OpenSearchActivity::class)
    }
}