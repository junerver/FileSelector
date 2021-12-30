package xyz.junerver.fsbrowser

import android.content.Intent
import xyz.junerver.fileselector.*

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
        mFileAdapter.setListener { path ->
          val ex = getExtension(path).lowercase()
            if (FileSelector.IMAGE_TYPES.contains(ex)) {
                //图片格式
                val i = Intent(this, PictureActivity::class.java)
                i.putExtra("path",path)
                startActivity(i)
            } else {
                openFile(path)
            }
        }
    }
}