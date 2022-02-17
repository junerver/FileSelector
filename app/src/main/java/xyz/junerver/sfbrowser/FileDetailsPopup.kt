package xyz.junerver.sfbrowser

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.lxj.xpopup.core.BottomPopupView
import xyz.junerver.fileselector.FileModel
import xyz.junerver.fileselector.formatFileSize
import xyz.junerver.fileselector.getDateTime
import xyz.junerver.fileselector.utils.AvatarUtils


/**
 * Description:
 * @author Junerver
 * date: 2021/12/30-14:42
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class FileDetailsPopup(context: Context, private val mFileModel: FileModel) :
    BottomPopupView(context) {
    override fun getImplLayoutId(): Int = R.layout.popup_file_details

    lateinit var mIvFileIcon: ImageView
    lateinit var mTvFileName: TextView
    lateinit var mTvFileSize: TextView
    lateinit var mTvFileDate: TextView
    lateinit var mTvFilePath: TextView
    lateinit var mBtnDone: Button
    override fun onCreate() {
        super.onCreate()
        mIvFileIcon = findViewById(R.id.iv_icon)
        mTvFileName = findViewById(R.id.tv_file_name)
        mTvFileSize = findViewById(R.id.tv_file_size)
        mTvFileDate = findViewById(R.id.tv_file_date)
        mTvFilePath = findViewById(R.id.tv_file_path)


        val extension = mFileModel.extension
        if (extension.equals("gif", ignoreCase = true)) {
            Glide.with(context).asGif().load(mFileModel.path).into(mIvFileIcon)
        } else if (extension.equals("jpg", ignoreCase = true) ||
            extension.equals("png", ignoreCase = true) ||
            extension.equals("bmp", ignoreCase = true) ||
            extension.equals("jpeg", ignoreCase = true) ||
            extension.equals("webp", ignoreCase = true) ||
            extension.equals("wmv", ignoreCase = true) ||
            extension.equals("flv", ignoreCase = true) ||
            extension.equals("mp4", ignoreCase = true) ||
            extension.equals("avi", ignoreCase = true) ||
            extension.equals("mpg", ignoreCase = true) ||
            extension.equals("rmvb", ignoreCase = true) ||
            extension.equals("rm", ignoreCase = true) ||
            extension.equals("asf", ignoreCase = true) ||
            extension.equals("f4v", ignoreCase = true) ||
            extension.equals("vob", ignoreCase = true) ||
            extension.equals("mkv", ignoreCase = true) ||
            extension.equals("3gp", ignoreCase = true) ||
            extension.equals("mov", ignoreCase = true)
        ) {
            Glide.with(context).load(mFileModel.path).into(mIvFileIcon)
        } else {
            val s = AvatarUtils.generateDefaultAvatar(context, extension)
            Glide.with(context).load(s).into(mIvFileIcon)
        }

        mTvFileName.text = "名称：${mFileModel.name}"
        mTvFileSize.text = "日期：${formatFileSize(mFileModel.size)}"
        mTvFilePath.text = "路径：${mFileModel.path}"
        mTvFileDate.text = "日期：${getDateTime(mFileModel.date)}"

        mBtnDone = findViewById(R.id.btn_done)
        mBtnDone.setOnClickListener {
            dismiss()
        }

    }
//
//    override fun getMaxHeight(): Int {
//        return ((XPopupUtils.getAppHeight(context) * .45f).roundToInt())
//    }
}