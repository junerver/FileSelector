package xyz.junerver.fileselector

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import xyz.junerver.fileselector.FileSelector.FilesScanCallBack
import xyz.junerver.fileselector.FileUtils.*
import xyz.junerver.fileselector.PermissionsUtils.PermissionsResult
import java.util.*


class FileSelectorActivity : AppCompatActivity() {
    private lateinit var recyclerView: FastScrollRecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var empty: TextView
    private val mFileModels = Collections.synchronizedList(ArrayList<FileModel>())
    private val mSelectedFileList = ArrayList<FileModel>()
    private var mCountMenuItem: MenuItem?=null
    private var mSelectSortTypeIndex = 0
    private lateinit var mFileAdapter: FileAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selector)
        recyclerView = findViewById(R.id.recycleView)
        progressBar = findViewById(R.id.progressBar)
        val mToolBar = findViewById<Toolbar>(R.id.toolbar)
        empty = findViewById(R.id.empty)
        window.statusBarColor = FileSelector.getInstance(this).barColor
        mToolBar.setBackgroundColor(FileSelector.getInstance(this).barColor)
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
        if (mCountMenuItem != null) {
            runOnUiThread {
                mCountMenuItem!!.title = String.format(
                    getString(R.string.selected_file_count),
                    mSelectedFileList.size.toString(),
                    FileSelector.getInstance(this@FileSelectorActivity).maxCount.toString()
                )
            }
        }
        FileSelector.getInstance(this).setThreadStop(false)
        val start = System.currentTimeMillis()
        FileSelector.getInstance(this).worker
            .setCallBack(object : FilesScanCallBack {
                override fun onNext(fileModels: List<FileModel>) {
                    "扫描到：${fileModels.size}个文件 ${Thread.currentThread().name}".log()
                    mFileModels.addAll(fileModels)
                    sortFileList(FileSelector.getInstance(this@FileSelectorActivity).mSortType)
                    runOnUiThread {
                        mFileAdapter.notifyDataSetChanged()
                        if (mFileModels.isEmpty()) {
                            empty.visible()
                            recyclerView.gone()
                        } else {
                            progressBar.gone()
                            empty.gone()
                            recyclerView.visible()
                        }
                    }
                }

                override fun onCompleted(fileModels: List<FileModel>) {
                    """
                        扫描完成，总数：${fileModels.size}个文件
                        校验：${mFileModels.size}
                    """.trimIndent().log()
                    sortFileList(FileSelector.getInstance(this@FileSelectorActivity).mSortType)
                    runOnUiThread {
                        mFileAdapter.notifyDataSetChanged()
                        if (mFileModels.isEmpty()) {
                            empty.visible()
                            recyclerView.gone()
                        } else {
                            progressBar.gone()
                            empty.gone()
                            recyclerView.visible()
                        }
                    }
                    val end = System.currentTimeMillis()
                    "全流程耗时: ${end - start} 文件总量： ${mFileModels.size}".log()
                }
            }).work()
    }

    private fun sortFileList(mSortType: Int) {
        mSelectedFileList.clear()
        try {
            when (mSortType) {
                FileUtils.BY_NAME_ASC -> {
                    Collections.sort(mFileModels, SortByName())
                }
                FileUtils.BY_NAME_DESC -> {
                    Collections.sort(mFileModels, SortByName())
                    mFileModels.reverse()
                }
                FileUtils.BY_TIME_ASC -> {
                    Collections.sort(mFileModels, SortByTime())
                }
                FileUtils.BY_TIME_DESC -> {
                    Collections.sort(mFileModels, SortByTime())
                    mFileModels.reverse()
                }
                FileUtils.BY_SIZE_ASC -> {
                    Collections.sort(mFileModels, SortBySize())
                }
                FileUtils.BY_SIZE_DESC -> {
                    Collections.sort(mFileModels, SortBySize())
                    mFileModels.reverse()
                }
                FileUtils.BY_EXTENSION_ASC -> {
                    Collections.sort(mFileModels, SortByExtension())
                }
                FileUtils.BY_EXTENSION_DESC -> {
                    Collections.sort(mFileModels, SortByExtension())
                    mFileModels.reverse()
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
        mCountMenuItem?.title = String.format(
            getString(R.string.selected_file_count),
            mSelectedFileList.size.toString(),
            FileSelector.getInstance(this@FileSelectorActivity).maxCount.toString()
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mFileAdapter == null) {
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
            if (FileSelector.getInstance(this@FileSelectorActivity).listener != null) {
                FileSelector.getInstance(this@FileSelectorActivity).listener.onResult(
                    mSelectedFileList
                )
            } else {
                val result = Intent()
                result.putParcelableArrayListExtra(FileUtils.RESULT_KEY, mSelectedFileList)
                setResult(RESULT_OK, result)
            }
            finish()
        } else if (i == R.id.browser_sort) {
            mSelectSortTypeIndex = 0
            AlertDialog.Builder(this)
                .setSingleChoiceItems(
                    R.array.sort_list,
                    0
                ) { _, which -> mSelectSortTypeIndex = which }
                .setNegativeButton("降序") { _, which ->
                    Thread {
                        runOnUiThread {
                            progressBar.visible()
                            recyclerView.gone()
                        }
                        when (mSelectSortTypeIndex) {
                            0 -> sortFileList(FileUtils.BY_NAME_DESC)
                            1 -> sortFileList(FileUtils.BY_TIME_DESC)
                            2 -> sortFileList(FileUtils.BY_SIZE_DESC)
                            3 -> sortFileList(FileUtils.BY_EXTENSION_DESC)
                            else -> {}
                        }
                        runOnUiThread {
                            mFileAdapter.notifyDataSetChanged()
                            progressBar.gone()
                            recyclerView.visible()
                        }
                    }.start()
                }
                .setPositiveButton("升序") { dialog, which ->
                    Thread {
                        runOnUiThread {
                            progressBar.visible()
                            recyclerView.gone()
                        }
                        when (mSelectSortTypeIndex) {
                            0 -> sortFileList(FileUtils.BY_NAME_ASC)
                            1 -> sortFileList(FileUtils.BY_TIME_ASC)
                            2 -> sortFileList(FileUtils.BY_SIZE_ASC)
                            3 -> sortFileList(FileUtils.BY_EXTENSION_ASC)
                            else -> {}
                        }
                        runOnUiThread {
                            mFileAdapter.notifyDataSetChanged()
                            progressBar.gone()
                            recyclerView.visible()
                        }
                    }.start()
                }
                .setTitle("请选择")
                .show()
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        FileSelector.getInstance(this).setThreadStop(true)
        if (FileSelector.getInstance(this).listener != null) {
            FileSelector.getInstance(this).listener.onCancel()
        }
        "onBackPressed: 尝试关闭页面".log()
        GlobalThreadPools.getInstance().shutdownNow()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        FileSelector.getInstance(this).mFileModelSet.clear()
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