package xyz.junerver.fileselector

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import info.debatty.java.stringsimilarity.RatcliffObershelp
import xyz.junerver.fileselector.worker.FilesScanWorker
import java.util.ArrayList

//置信度
const val CONFIDENCE_LEVEL = 0.05
const val EXTRA_IS_SELECTOR_MODE = "isSelectorMode"

/**
 * Description: 文件搜索页面
 * @author Junerver
 * @Email: junerver@gmail.com
 */
open class FileSearchActivity : AppCompatActivity(), OperateFileModelItem {

    private lateinit var recyclerView: FastScrollRecyclerView
    private lateinit var empty: TextView
    private lateinit var root: CoordinatorLayout

    //用于显示搜索结果
    private val mResult = ArrayList<FileModel>()
    protected open lateinit var mFileAdapter: FileAdapter
    private val mSelectedFileList = ArrayList<FileModel>()
    private val ro = RatcliffObershelp()
    var mSearchItem: MenuItem? = null
    var mSearchView: SearchView? = null
    protected open lateinit var mContext: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_search)
        mContext = this
        root = findViewById(R.id.root)
        val mToolBar = findViewById<Toolbar>(R.id.toolbar)
        window.statusBarColor = FileSelector.barColor
        mToolBar.setBackgroundColor(FileSelector.barColor)
        setSupportActionBar(mToolBar)
        recyclerView = findViewById(R.id.recycleView)
        empty = findViewById(R.id.empty)

        initAdapter()
    }

    protected open fun initAdapter() {
        mFileAdapter = FileAdapter(
            this,
            R.layout.item_file_selector,
            mResult,
            intent.getBooleanExtra(EXTRA_IS_SELECTOR_MODE, true)
        )
        mFileAdapter.setSelectedFileList(mSelectedFileList)
        mFileAdapter.setMaxSelect(intent.getIntExtra("remainder", 0))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mFileAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        mSearchItem = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView = mSearchItem?.actionView as SearchView?
        mSearchView?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconified = true
            this.setIconifiedByDefault(false)
            isSubmitButtonEnabled = true
            inputType = 1 // .setImeOptions(SearchView.);
            isFocusableInTouchMode = true
            findViewById<TextView>(R.id.search_src_text).hint = "搜索..."
            findViewById<TextView>(R.id.search_src_text).addTextChangedListenerDsl {
                afterTextChanged {
                    it?.toString()?.let {
                        mResult.clear()
                        mResult.addAll(FilesScanWorker.mFileModelSet.toList().filter { fm ->
                            val similarity = ro.similarity(it, fm.name)
                            fm.similarity = similarity
                            //避免复杂逻辑，只筛选未选中的文件
                            similarity > CONFIDENCE_LEVEL && !fm.isSelected
                        })
                        mResult.sortByDescending { r -> r.similarity }
                        mFileAdapter.notifyDataSetChanged()
                    }
                }
            }
            findViewById<ImageView>(R.id.search_go_btn).apply {
                Glide.with(this).load(R.drawable.ic_confirm).into(this)
                setOnClickListener {
                    finish()
                }
            }
        }
        mSearchItem?.expandActionView()
        mSearchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                finish()
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        clearFocus()
        SoftKeyBoardListener.setListener(
            this,
            object : SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
                override fun keyBoardShow(height: Int) {}

                override fun keyBoardHide(height: Int) {
                    clearFocus()
                }
            })
    }

    private fun clearFocus() {
        mSearchView?.let {
            it.clearFocus()
            root.apply {
                isFocusableInTouchMode = true
                requestFocus()
            }
        }
    }

    override fun delItem(fileModel: FileModel) {
        val index = mResult.indexOf(fileModel)
        if (index != -1) {
            mResult.remove(fileModel)
            mFileAdapter.notifyItemRemoved(index)
        }
    }

    override fun changeItem(fileModel: FileModel) {
        val index = mResult.indexOf(fileModel)
        if (index != -1) {
            mFileAdapter.notifyItemChanged(index)
        }
    }

}