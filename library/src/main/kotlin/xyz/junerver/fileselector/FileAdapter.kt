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
import java.text.DecimalFormat
import java.util.*

/**
 * @author Lee
 */
internal class FileAdapter(context: Context?, layoutId: Int, private val modelList: List<FileModel>) :
    CommonAdapter<FileModel>(context, layoutId, modelList), SectionedAdapter {

    private var mMaxSelect = FileSelector.maxCount

    private var mCountMenuItem: MenuItem? = null
    private var mSelectedFileList: ArrayList<FileModel>? = null

    fun setMaxSelect(max: Int) {
        mMaxSelect = max
    }

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
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(fileModel.isSelected, false)
        checkBox.setOnCheckedChangeListener(SmoothCheckBox.OnCheckedChangeListener { cb, isChecked ->
            if (!isChecked && fileModel.isSelected) {
                val index = findFileIndex(fileModel)
                if (index != -1) {
                    mSelectedFileList!!.removeAt(index)
                }
                fileModel.isSelected = false
            } else if (isChecked && !fileModel.isSelected) {
                if (mSelectedFileList!!.size >= mMaxSelect) {
                    Toast.makeText(
                        mContext,
                        "您最多只能选择" + FileSelector.maxCount.toString() + "个",
                        Toast.LENGTH_SHORT
                    ).show()
                    cb.setChecked(false, true)
                    return@OnCheckedChangeListener
                }
                "onCheckedChanged: ${fileModel.name}".log()
                mSelectedFileList!!.add(fileModel)
                fileModel.isSelected = true
            }
            mCountMenuItem?.title = String.format(
                mContext.getString(R.string.selected_file_count),
                mSelectedFileList!!.size.toString(),
                java.lang.String.valueOf(FileSelector.maxCount)
            )
        })
        layout.setOnClickListener {
            if (fileModel.isSelected) {
                val index = findFileIndex(fileModel)
                if (index != -1) {
                    mSelectedFileList!!.removeAt(index)
                }
                fileModel.isSelected = false
            } else {
                if (mSelectedFileList!!.size >= mMaxSelect) {
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
        return namePinyin.substring(0, 1).uppercase()
    }

    private fun formatFileSize(size: Long): String {
        val df = DecimalFormat("#.00")
        val fileSizeString: String = when {
            size < 1024 -> {
                df.format(size.toDouble()) + "B"
            }
            size < 1048576 -> {
                df.format(size.toDouble() / 1024) + "K"
            }
            size < 1073741824 -> {
                df.format(size.toDouble() / 1048576) + "M"
            }
            else -> {
                df.format(size.toDouble() / 1073741824) + "G"
            }
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
}