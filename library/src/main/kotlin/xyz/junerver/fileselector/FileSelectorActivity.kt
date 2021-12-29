package xyz.junerver.fileselector

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

const val RESULT_KEY = "extra_result"

class FileSelectorActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: FastScrollRecyclerView
    private lateinit var empty: TextView
    private val mFileModels = ArrayList<FileModel>()
    private val mSelectedFileList = ArrayList<FileModel>()
    private var mCountMenuItem: MenuItem? = null

    //用户选择的排序方式索引
    private var mSelectSortTypeIndex = 0
    private lateinit var mFileAdapter: FileAdapter

    //用户当前选择的排序方式
    private var mCurrentSortType = FileSelector.mSortType


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selector)
        recyclerView = findViewById(R.id.recycleView)
        progressBar = findViewById(R.id.progressBar)
        val mToolBar = findViewById<Toolbar>(R.id.toolbar)
        empty = findViewById(R.id.empty)
        window.statusBarColor = FileSelector.barColor
        mToolBar.setBackgroundColor(FileSelector.barColor)
        setSupportActionBar(mToolBar)
        supportActionBar?.title = "文件选择"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        mToolBar.setNavigationOnClickListener { onBackPressed() }
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        PermissionsUtils.getInstance()
            .checkPermissions(this, permissions, object : PermissionsResult {
                override fun passPermission() {
                    progressBar.visible()
                    initAdapter()
                    getFiles()
                }

                override fun continuePermission() {
                    Toast.makeText(this@FileSelectorActivity, "读写权限被拒绝", Toast.LENGTH_LONG).show()
                }

                override fun refusePermission() {
                    Toast.makeText(this@FileSelectorActivity, "读写权限被拒绝", Toast.LENGTH_LONG).show()
                }
            })
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
                        progressBar.gone()
                        empty.gone()
                        recyclerView.visible()
                    }
                }

                override fun onCompleted(fileModels: List<FileModel>) {

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

    private fun initAdapter() {
        mFileAdapter = FileAdapter(this, R.layout.item_file_selector, mFileModels)
        mFileAdapter.setSelectedFileList(mSelectedFileList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mFileAdapter
        if (mCountMenuItem != null) {
            mFileAdapter.setCountMenuItem(mCountMenuItem)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.selector_menu, menu)
        mCountMenuItem = menu.findItem(R.id.select_count)
        if (this::mFileAdapter.isInitialized) {
            "adapter init complete".log()
            mFileAdapter.setCountMenuItem(mCountMenuItem)
        }
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
                ActivityUIWorker.listener!!.onResult(
                    mSelectedFileList
                )
            }
            finish()
        } else if (i == R.id.browser_sort) {
            val i = Intent(this, FileSearchActivity::class.java)
            //还能选多少
            i.putExtra("remainder", FileSelector.maxCount - mSelectedFileList.size)
            startActivityForResult(i,999)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 999) {
            val list = data?.getStringArrayListExtra(RESULT_KEY)
        }
        mSelectedFileList.clear()
        mSelectedFileList.addAll(mFileModels.filter { it.isSelected })
        mFileAdapter.notifyDataSetChanged()
        updateMenuUI()
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