package xyz.junerver.fileselector

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import xyz.junerver.fileselector.FileSelector.Companion.BY_EXTENSION_ASC
import xyz.junerver.fileselector.FileSelector.Companion.BY_EXTENSION_DESC
import xyz.junerver.fileselector.FileSelector.Companion.BY_NAME_ASC
import xyz.junerver.fileselector.FileSelector.Companion.BY_NAME_DESC
import xyz.junerver.fileselector.FileSelector.Companion.BY_SIZE_ASC
import xyz.junerver.fileselector.FileSelector.Companion.BY_SIZE_DESC
import xyz.junerver.fileselector.FileSelector.Companion.BY_TIME_ASC
import xyz.junerver.fileselector.FileSelector.Companion.BY_TIME_DESC
import xyz.junerver.fileselector.PermissionsUtils.PermissionsResult
import xyz.junerver.fileselector.worker.FilesScanWorker
import xyz.junerver.fileselector.worker.ActivityUIWorker
import java.util.*
import kotlin.reflect.KClass


const val RESULT_KEY = "extra_result"

//请求管理全部文件
const val REQUEST_CODE_MANAGE_APP_ALL_FILES = 998

//请求查找文件
const val REQUEST_CODE_SEARCH_FILES = 998

open class FileSelectorActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: FastScrollRecyclerView
    private lateinit var empty: TextView
    protected open lateinit var mToolBar:Toolbar

    private val mFileModels = ArrayList<FileModel>()
    private val mSelectedFileList = ArrayList<FileModel>()
    private var mCountMenuItem: MenuItem? = null

    //用户选择的排序方式索引
    private var mSelectSortTypeIndex = 0
    protected lateinit var mFileAdapter: FileAdapter
    //是否为选择器模式
    protected open var isSelectorMode = true

    //用户当前选择的排序方式
    private var mCurrentSortType = FileSelector.mSortType
    private lateinit var mContext: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selector)
        mContext = this
        recyclerView = findViewById(R.id.recycleView)
        progressBar = findViewById(R.id.progressBar)
        empty = findViewById(R.id.empty)
        initToolBar()
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        PermissionsUtils.getInstance()
            .checkPermissions(this, permissions, object : PermissionsResult {
                override fun passPermission() {
                    progressBar.visible()
                    initAdapter()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                        //已有文件管理权限
                        getFiles()
                    } else {
                        //没有文件管理权限去申请
                        showManagerFileTips(
                            cancel = { getFiles() },
                            request = {
                                startActivityForResult(
                                    it,
                                    REQUEST_CODE_MANAGE_APP_ALL_FILES
                                )
                            }
                        )
                    }
                }

                override fun continuePermission() {
                    Toast.makeText(this@FileSelectorActivity, "读写权限被拒绝", Toast.LENGTH_LONG).show()
                }

                override fun refusePermission() {
                    Toast.makeText(this@FileSelectorActivity, "读写权限被拒绝", Toast.LENGTH_LONG).show()
                }
            })
    }

    protected open fun initToolBar(title: String = "文件选择") {
        mToolBar = findViewById<Toolbar>(R.id.toolbar)
        window.statusBarColor = FileSelector.barColor
        mToolBar.setBackgroundColor(FileSelector.barColor)
        setSupportActionBar(mToolBar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        mToolBar.setNavigationOnClickListener { onBackPressed() }
    }

    protected open fun initAdapter() {
        mFileAdapter = FileAdapter(this, R.layout.item_file_selector, mFileModels,isSelectorMode)
        mFileAdapter.setSelectedFileList(mSelectedFileList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mFileAdapter
        if (mCountMenuItem != null) {
            mFileAdapter.setCountMenuItem(mCountMenuItem)
        }
    }

    protected open  fun openSearchUI(clazz: KClass<out FileSearchActivity> = FileSearchActivity::class ) {
        val i = Intent(this, clazz.java)
        //还能选多少
        i.putExtra("remainder", FileSelector.maxCount - mSelectedFileList.size)
        i.putExtra(EXTRA_IS_SELECTOR_MODE, isSelectorMode)
        startActivityForResult(i, REQUEST_CODE_SEARCH_FILES)
    }

    private fun getFiles() {
        mFileModels.clear()
        updateMenuUI()
        val start = System.currentTimeMillis()
        FilesScanWorker
            .setCallBack(object : FilesScanWorker.FilesScanCallBack {
                override fun onNext(fileModels: List<FileModel>) {
                    "scanned：${fileModels.size} ".log()
                    sortFileList(mCurrentSortType, fileModels as ArrayList<FileModel>)
                    val lastIndex = mFileModels.size
                    mFileModels.addAll(fileModels)
                    mFileAdapter.notifyItemRangeInserted(lastIndex, fileModels.size)
                    if (mFileModels.isEmpty()) {
                        empty.visible()
                        recyclerView.gone()
                    } else {
                        empty.gone()
                        recyclerView.visible()
                    }
                    progressBar.gone()
                }

                override fun onCompleted(fileModels: List<FileModel>) {
                    if (mFileModels.isEmpty()) {
                        empty.visible()
                        recyclerView.gone()

                    } else {
                        empty.gone()
                        recyclerView.visible()
                    }
                    progressBar.gone()
                    val end = System.currentTimeMillis()
                    "scan completed，total：${fileModels.size}  All time consumed: ${end - start}ms ".log()
                }
            }).work()
    }

    private fun sortFileList(sortType: Int, list: ArrayList<FileModel>) {
        if (sortType == -1) {
            return
        }
        mCurrentSortType = sortType
        try {
            when (sortType) {
                BY_NAME_ASC -> {
                    list.sortBy { it.name }
                }
                BY_NAME_DESC -> {
                    list.sortByDescending { it.name }
                }
                BY_TIME_ASC -> {
                    list.sortBy { it.date }
                }
                BY_TIME_DESC -> {
                    list.sortByDescending { it.date }
                }
                BY_SIZE_ASC -> {
                    list.sortBy { it.size }
                }
                BY_SIZE_DESC -> {
                    list.sortByDescending { it.size }
                }
                BY_EXTENSION_ASC -> {
                    list.sortBy { it.extension }
                }
                BY_EXTENSION_DESC -> {
                    list.sortByDescending { it.extension }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.selector_menu, menu)
        mCountMenuItem = menu.findItem(R.id.select_count)
        if (this::mFileAdapter.isInitialized) {
            "adapter init complete".log()
            mFileAdapter.setCountMenuItem(mCountMenuItem)
        }
        mCountMenuItem?.isVisible = isSelectorMode
        updateMenuUI()
        return true
    }

    private fun updateMenuUI() {
        mCountMenuItem?.title = String.format(
            getString(R.string.selected_file_count),
            mSelectedFileList.size.toString(),
            FileSelector.maxCount.toString()
        )
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!this::mFileAdapter.isInitialized) {
            toast("请等待文件加载完毕")
            return true
        }
        val i = item.itemId
        if (i == R.id.select_count) {
            //选中
            if (mSelectedFileList.isEmpty()) {
                toast("未选择任何文件")
                return true
            }
            //不为空
            if (ActivityUIWorker.listener != null) {
                ActivityUIWorker.listener?.onResult(
                    mSelectedFileList
                )
            }
            finish()
        } else if (i == R.id.search_file) {
            openSearchUI()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SEARCH_FILES) {
            mSelectedFileList.clear()
            mSelectedFileList.addAll(mFileModels.filter { it.isSelected })
            mFileAdapter.notifyDataSetChanged()
            updateMenuUI()
        }
        if (requestCode == REQUEST_CODE_MANAGE_APP_ALL_FILES && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                toast("文件访问权限获取失败，部分文件可能无法展示！")
            }
            getFiles()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (ActivityUIWorker.listener != null) {
            ActivityUIWorker.listener!!.onCancel()
        }
        GlobalThreadPools.getInstance().shutdownNow()
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.getInstance()
            .onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }
}