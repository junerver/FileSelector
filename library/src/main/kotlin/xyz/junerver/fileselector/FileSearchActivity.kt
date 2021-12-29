package xyz.junerver.fileselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import info.debatty.java.stringsimilarity.RatcliffObershelp
import xyz.junerver.fileselector.worker.FilesScanWorker
import java.util.ArrayList

const val SIMILARITY = 0.05

class FileSearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: FastScrollRecyclerView
    private lateinit var empty: TextView

    //用于显示搜索结果
    private val mResult = ArrayList<FileModel>()
    private lateinit var mFileAdapter: FileAdapter
    private val mSelectedFileList = ArrayList<FileModel>()
    val ro = RatcliffObershelp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_search)
        val mToolBar = findViewById<Toolbar>(R.id.toolbar)
        window.statusBarColor = FileSelector.barColor
        mToolBar.setBackgroundColor(FileSelector.barColor)
        setSupportActionBar(mToolBar)

        recyclerView = findViewById(R.id.recycleView)
        empty = findViewById(R.id.empty)

        mFileAdapter = FileAdapter(this, R.layout.item_file_selector, mResult)
        mFileAdapter.setSelectedFileList(mSelectedFileList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mFileAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView?
        searchView?.apply {
            isIconified = true
            this.setIconifiedByDefault(false)
            isSubmitButtonEnabled = true
            inputType = 1 // .setImeOptions(SearchView.);
            findViewById<TextView>(R.id.search_src_text).addTextChangedListenerDsl {
                afterTextChanged {
                    val s = it?.toString()
                    s?.log()
                    s?.let {
                        mResult.clear()
                        mResult.addAll(FilesScanWorker.mFileModelSet.toList().filter { fm ->
                            val sim = ro.similarity(it, fm.name)
                            fm.similarity = sim
                            sim > SIMILARITY
                        })
                        mResult.sortByDescending { r -> r.similarity }
                        mFileAdapter.notifyDataSetChanged()
                    }

                }
            }
            findViewById<ImageView>(R.id.search_go_btn).setOnClickListener {
                "当前选择了：${mSelectedFileList.size} \n${mSelectedFileList.joinToString { it.name + "\n" }}".log()
            }
        }
        searchItem?.expandActionView()
        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                toast("案秀云Expand!")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                finish()
                return true
            }
        })

        return true
    }

}