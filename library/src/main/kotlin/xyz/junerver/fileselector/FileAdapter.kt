package xyz.junerver.fileselector

import android.content.Context
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lee.adapter.recyclerview.CommonAdapter
import com.lee.adapter.recyclerview.base.ViewHolder
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import xyz.junerver.fileselector.FileUtils.getDateTime
import java.text.DecimalFormat
import java.util.*

/**
 * @author Lee
 */
internal class FileAdapter(context: Context?, layoutId: Int, private val modelList: List<FileModel>) :
    CommonAdapter<FileModel>(context, layoutId, modelList), SectionedAdapter {
    private var mCountMenuItem: MenuItem? = null
    private var mSelectedFileList: ArrayList<FileModel>? = null
    fun setCountMenuItem(mCountMenuItem: MenuItem?) {
        this.mCountMenuItem = mCountMenuItem
    }

    fun setSelectedFileList(mSelectedFileList: ArrayList<FileModel>?) {
        this.mSelectedFileList = mSelectedFileList
    }

    override fun convert(holder: ViewHolder, fileModel: FileModel, position: Int) {
        val imageView = holder.getView<ImageView>(R.id.iv_type)
        val extension = fileModel.extension
        if (extension.equals("gif", ignoreCase = true)) {
            Glide.with(mContext).asGif().load(fileModel.path).into(imageView)
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
            Glide.with(mContext).load(fileModel.path).into(imageView)
        } else {
            val s = AvatarUtils.generateDefaultAvatar(mContext, extension)
            Glide.with(mContext).load(s).into(imageView)
        }
        val checkBox = holder.getView<SmoothCheckBox>(R.id.checkbox)
        val layout = holder.getView<RelativeLayout>(R.id.layout_item)
        holder.setText(R.id.tv_name, fileModel.name)
        holder.setText(
            R.id.tv_detail,
            getDateTime(fileModel.date) + "  -  " + formatFileSize(fileModel.size)
        )
        checkBox.apply {
            setOnCheckedChangeListener(null)
            setChecked(fileModel.isSelected, false)
            setOnCheckedChangeListener(SmoothCheckBox.OnCheckedChangeListener { checkBox, isChecked ->
                if (!isChecked && fileModel.isSelected) {
                    val index = findFileIndex(fileModel)
                    if (index != -1) {
                        mSelectedFileList!!.removeAt(index)
                    }
                    fileModel.isSelected = false
                } else if (isChecked && !fileModel.isSelected) {
                    if (mSelectedFileList!!.size >= FileSelector.maxCount) {
                        Toast.makeText(
                            mContext,
                            "您最多只能选择" + FileSelector.maxCount.toString() + "个",
                            Toast.LENGTH_SHORT
                        ).show()
                        checkBox.setChecked(false, true)
                        return@OnCheckedChangeListener
                    }
                    Log.d(TAG, "onCheckedChanged: " + fileModel.name)
                    mSelectedFileList!!.add(fileModel)
                    fileModel.isSelected = true
                }
                mCountMenuItem!!.title = String.format(
                    mContext.getString(R.string.selected_file_count),
                    mSelectedFileList!!.size.toString(),
                    java.lang.String.valueOf(FileSelector.maxCount)
                )
            })
        }
        layout.setOnClickListener { v: View? ->
            if (fileModel.isSelected) {
                val index = findFileIndex(fileModel)
                if (index != -1) {
                    mSelectedFileList!!.removeAt(index)
                }
                fileModel.isSelected = false
            } else {
                if (mSelectedFileList!!.size >= FileSelector.maxCount) {
                    Toast.makeText(
                        mContext,
                        "您最多只能选择" + FileSelector.maxCount.toString() + "个",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                mSelectedFileList!!.add(fileModel)
                fileModel.isSelected = true
            }
            checkBox.setChecked(fileModel.isSelected, true)
            mCountMenuItem?.title = String.format(
                mContext.getString(R.string.selected_file_count),
                mSelectedFileList!!.size.toString(),
                java.lang.String.valueOf(FileSelector.maxCount)
            )
        }
    }

    override fun getSectionName(position: Int): String {
        val namePinyin = CharacterParser.getInstance().getSpelling(
            modelList[position].name
        )
        return namePinyin.substring(0, 1).toUpperCase()
    }

    private fun formatFileSize(size: Long): String {
        val df = DecimalFormat("#.00")
        val fileSizeString: String = if (size < 1024) {
            df.format(size.toDouble()) + "B"
        } else if (size < 1048576) {
            df.format(size.toDouble() / 1024) + "K"
        } else if (size < 1073741824) {
            df.format(size.toDouble() / 1048576) + "M"
        } else {
            df.format(size.toDouble() / 1073741824) + "G"
        }
        return fileSizeString
    }

    private fun findFileIndex(item: FileModel): Int {
        for (i in mSelectedFileList!!.indices) {
            if (mSelectedFileList!![i].path == item.path) {
                return i
            }
        }
        return -1
    }

    companion object {
        private const val TAG = "FileAdapter"
    }
}