package xyz.junerver.sfbrowser

import xyz.junerver.fileselector.FileSearchActivity

/**
 * Description:
 * @author Junerver
 * date: 2021/12/30-11:11
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class OpenSearchActivity : FileSearchActivity() {
    override fun initAdapter() {
        super.initAdapter()
        mFileAdapter.setListener(BrowserItemOnClickListenerImpl(this))
    }
}