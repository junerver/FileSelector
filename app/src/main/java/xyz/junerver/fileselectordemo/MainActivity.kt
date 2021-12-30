package xyz.junerver.fileselectordemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import xyz.junerver.fileselector.FileSelector
import xyz.junerver.fileselector.FileSelectorActivity
import xyz.junerver.fileselector.R

class MainActivity : FileSelectorActivity() {

    override var isSelectorMode = true

    override fun initToolBar(title: String) {
       super.initToolBar("文件浏览")
        mToolBar.navigationIcon = null
        mToolBar.setNavigationOnClickListener { }
    }
}