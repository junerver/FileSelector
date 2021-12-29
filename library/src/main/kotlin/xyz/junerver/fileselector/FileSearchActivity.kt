package xyz.junerver.fileselector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import info.debatty.java.stringsimilarity.RatcliffObershelp
import xyz.junerver.fileselector.worker.FilesScanWorker
import java.util.ArrayList

//置信度
const val CONFIDENCE_LEVEL = 0.05

class FileSearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: FastScrollRecyclerView
    private lateinit var empty: TextView

    //用于显示搜索结果
    private val mResult = ArrayList<FileModel>()
    private lateinit var mFileAdapter: FileAdapter
    private val mSelectedFileList = ArrayList<FileModel>()
    private val ro = RatcliffObershelp()

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
        mFileAdapter.setMaxSelect(intent.getIntExtra("remainder", 0))
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
                    "selected：${mSelectedFileList.size} \n${mSelectedFileList.joinToString { it.name }}".log()
                    val result = Intent()
                    result.putStringArrayListExtra(
                        RESULT_KEY,
                        ArrayList(mSelectedFileList.map { it.path })
                    )
                    setResult(RESULT_OK, result)
                    finish()
                }
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