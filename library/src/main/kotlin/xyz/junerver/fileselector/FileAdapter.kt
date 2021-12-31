package xyz.junerver.fileselector

import android.content.Context
import android.view.MenuItem
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lee.adapter.recyclerview.CommonAdapter
import com.lee.adapter.recyclerview.base.ViewHolder
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import java.util.*

/**
 * @author Lee
 */
open class FileAdapter(
    context: Context?,
    layoutId: Int,
    private val modelList: List<FileModel>,
    private val isSelectorMode: Boolean = false
) :
    CommonAdapter<FileModel>(context, layoutId, modelList), SectionedAdapter {

    //最大选择数量
    private var mMaxSelect = FileSelector.maxCount

    //主UI的右上角menu
    private var mCountMenuItem: MenuItem? = null

    //选择的文件
    private var mSelectedFileList: ArrayList<FileModel>? = null

    interface BrowserItemOnClickListener {
        fun onItemClick(ctx: Context, fileModel: FileModel)
        fun onItemLongClick(ctx: Context, fileModel: FileModel)
    }

    private var mBrowserItemOnClickListener: BrowserItemOnClickListener? = null

    fun setListener(onClickListener: BrowserItemOnClickListener) {
        mBrowserItemOnClickListener = onClickListener
    }

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
        holder.apply {
            setText(R.id.tv_name, fileModel.name)
            setText(
                R.id.tv_detail,
                getDateTime(fileModel.date) + "  -  " + formatFileSize(fileModel.size)
            )
        }
        //勾选框配置
        val checkBox = holder.getView<SmoothCheckBox>(R.id.checkbox)
        checkBox.apply {
            setOnCheckedChangeListener(null)
            setChecked(fileModel.isSelected, false)
            setOnCheckedChangeListener(SmoothCheckBox.OnCheckedChangeListener { cb, isChecked ->
                if (!isChecked && fileModel.isSelected) {
                    mSelectedFileList?.remove(fileModel)
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
            if (isSelectorMode) {
                visible()
            } else {
                gone()
            }
        }
        val flag = holder.getView<ImageView>(R.id.iv_data_flag)
        flag.apply {
            if (fileModel.isAndroidData) {
                visible()
            } else {
                gone()
            }
        }
        val layout = holder.getView<RelativeLayout>(R.id.layout_item)
        layout.apply {
            setOnClickListener {
                if (isSelectorMode) {
                    if (fileModel.isSelected) {
                        mSelectedFileList?.remove(fileModel)
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
                } else {
                    //打开
                    mBrowserItemOnClickListener?.onItemClick(it.context, fileModel)
                }

            }

            setOnLongClickListener {
                if (!isSelectorMode) {
                    mBrowserItemOnClickListener?.onItemLongClick(it.context, fileModel)
                }
                true
            }
        }

    }

    override fun getSectionName(position: Int): String {
        val namePinyin = CharacterParser.getInstance().getSpelling(
            modelList[position].name
        )
        return namePinyin.substring(0, 1).uppercase()
    }
}