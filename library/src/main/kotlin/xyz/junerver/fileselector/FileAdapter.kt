package xyz.junerver.fileselector

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import xyz.junerver.fileselector.utils.AvatarUtils
import xyz.junerver.fileselector.utils.CharacterParserUtils
import xyz.junerver.fileselector.worker.DIND_TALK_PATH
import java.util.ArrayList

/**
 * Description:
 * @author Junerver
 * date: 2022/2/16-14:53
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class FileAdapter(
    private val mContext: Context,
    private val layoutId: Int,
    private val modelList: List<FileModel>,
    //是否为选择器模式
    private val isSelectorMode: Boolean = false
) : RecyclerView.Adapter<FileAdapter.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    //最大选择数量
    private var mMaxSelect = FileSelector.maxCount

    private val mFileTypeBitmapMap = HashMap<String, String>()

    //主UI的右上角menu
    private var mCountMenuItem: MenuItem? = null

    //选择的文件
    private var mSelectedFileList: ArrayList<FileModel>? = null

    interface BrowserItemOnClickListener {
        fun onItemClick(ctx: Context, holder: ViewHolder, fileModel: FileModel)
        fun onItemLongClick(ctx: Context, holder: ViewHolder, fileModel: FileModel)
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

    override fun getSectionName(position: Int): String {
        val namePinyin = CharacterParserUtils.getSpelling(
            modelList[position].name
        )
        return namePinyin.substring(0, 1).uppercase()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val root = mContext.inflater(R.layout.item_file_selector, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileModel = modelList[position]
        val imageView = holder.imageView
        val extension = fileModel.extension
        if (extension.equals("gif", ignoreCase = true) ||
            extension.equals("jpg", ignoreCase = true) ||
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
            if (fileModel.isAndroidData) {
                imageView.load(fileModel.uri)
            } else {
                imageView.load(fileModel.path)
            }
        } else {
            val s = if (mFileTypeBitmapMap.containsKey(extension)) {
                mFileTypeBitmapMap[extension]
            } else {
                val bitmapPath = AvatarUtils.generateDefaultAvatar(mContext, extension)
                mFileTypeBitmapMap[extension] = bitmapPath
                bitmapPath
            }
            Glide.with(mContext).load(s).into(imageView)
        }
        holder.apply {
            name.text = fileModel.name
            detail.text = "${getDateTime(fileModel.date)}  -  ${formatFileSize(fileModel.size)}"
        }
        //勾选框配置
        val checkBox = holder.checkBox
        checkBox.apply {
            setOnCheckedChangeListener(null)
            setChecked(fileModel.isSelected, false)
            setOnCheckedChangeListener(SmoothCheckBox.OnCheckedChangeListener { cb, isChecked ->
                if (!isChecked && fileModel.isSelected) {
                    mSelectedFileList?.remove(fileModel)
                    fileModel.isSelected = false
                } else if (isChecked && !fileModel.isSelected) {
                    if (mSelectedFileList?.size?.let { it >= mMaxSelect } == true) {
                        Toast.makeText(
                            mContext,
                            "您最多只能选择" + FileSelector.maxCount.toString() + "个",
                            Toast.LENGTH_SHORT
                        ).show()
                        cb.setChecked(false, true)
                        return@OnCheckedChangeListener
                    }
                    "onCheckedChanged: ${fileModel.name}".log()
                    mSelectedFileList?.also {
                        it += fileModel
                    }
                    fileModel.isSelected = true
                }
                mCountMenuItem?.title = String.format(
                    mContext.getString(R.string.selected_file_count),
                    mSelectedFileList?.size ?: "0",
                    java.lang.String.valueOf(FileSelector.maxCount)
                )
            })
            if (isSelectorMode) {
                visible()
            } else {
                gone()
            }
        }
        //data目录标志
        val flag = holder.flag
        flag.apply {
            if (fileModel.isAndroidData) {
                FileSelector.ICON_MAP[getFileParentPath(fileModel.path)]?.let {
                    setImageDrawable(this.context.getDrawableRes(it))
                    this.visible()
                } ?: run { gone() }
            } else {
                if (fileModel.path.contains(DIND_TALK_PATH)) {
                    setImageDrawable(this.context.getDrawableRes(R.drawable.icon_dingding))
                    this.visible()
                } else {
                    gone()
                }
            }
        }
        val layout = holder.layout
        layout.apply {
            setOnClickListener {
                if (isSelectorMode) {
                    if (fileModel.isSelected) {
                        mSelectedFileList?.remove(fileModel)
                        fileModel.isSelected = false
                    } else {
                        if (mSelectedFileList?.size?.let { size -> size >= mMaxSelect } == true) {
                            Toast.makeText(
                                mContext,
                                "您最多只能选择" + FileSelector.maxCount.toString() + "个",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                        mSelectedFileList?.also { list -> list += fileModel }
                        fileModel.isSelected = true
                    }
                    checkBox.setChecked(fileModel.isSelected, true)
                    mCountMenuItem?.title = String.format(
                        mContext.getString(R.string.selected_file_count),
                        mSelectedFileList?.size ?: "0",
                        java.lang.String.valueOf(FileSelector.maxCount)
                    )
                } else {
                    //打开
                    mBrowserItemOnClickListener?.onItemClick(it.context, holder, fileModel)
                }
            }

            setOnLongClickListener {
                if (!isSelectorMode) {
                    mBrowserItemOnClickListener?.onItemLongClick(it.context, holder, fileModel)
                }
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.imageView.apply {
            this.setImageDrawable(null)
            Glide.with(this).clear(this)
        }
        super.onViewRecycled(holder)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_type)
        val checkBox: SmoothCheckBox = itemView.findViewById(R.id.checkbox)
        val flag: ImageView = itemView.findViewById(R.id.iv_data_flag)
        val layout: RelativeLayout = itemView.findViewById(R.id.layout_item)
        val name: TextView = itemView.findViewById(R.id.tv_name)
        val detail: TextView = itemView.findViewById(R.id.tv_detail)
        val circularProgressBar: CircularProgressBar =
            itemView.findViewById(R.id.circularProgressBar)
    }
}