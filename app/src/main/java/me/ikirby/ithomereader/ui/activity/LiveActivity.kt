package me.ikirby.ithomereader.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.list_layout.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import me.ikirby.ithomereader.R
import me.ikirby.ithomereader.api.impl.LiveApiImpl
import me.ikirby.ithomereader.entity.LiveMsg
import me.ikirby.ithomereader.ui.adapter.LivePostListAdapter
import me.ikirby.ithomereader.ui.base.BaseActivity
import me.ikirby.ithomereader.ui.util.ToastUtil
import me.ikirby.ithomereader.ui.util.UiUtil
import me.ikirby.ithomereader.util.getMatchInt

class LiveActivity : BaseActivity() {

    private lateinit var liveMessages: ArrayList<LiveMsg>
    private lateinit var adapter: LivePostListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var url: String
    private lateinit var newsId: String
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleCustom(getString(R.string.live))
        enableBackBtn()

        url = intent.getStringExtra("url")
        newsId = "" + getMatchInt(url)

        layoutManager = LinearLayoutManager(this)
        list_view.layoutManager = layoutManager

        swipe_refresh.setColorSchemeResources(UiUtil.getAccentColorRes())
        swipe_refresh.setProgressBackgroundColorSchemeResource(UiUtil.getWindowBackgroundColorRes())
        swipe_refresh.setOnRefreshListener { loadList() }

        error_placeholder.setOnClickListener { loadList() }

        if (savedInstanceState != null) {
            liveMessages = savedInstanceState.getParcelableArrayList("liveMessages") ?: ArrayList()
        }

        if (savedInstanceState == null || liveMessages.isEmpty()) {
            liveMessages = ArrayList()
            adapter = LivePostListAdapter(liveMessages, layoutInflater)
            list_view.adapter = adapter
            loadList()
        } else {
            adapter = LivePostListAdapter(liveMessages, layoutInflater)
            list_view.adapter = adapter
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("list_state"))
        }
    }

    override fun initView() {
        setContentView(R.layout.list_layout)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("liveMessages", liveMessages)
        outState.putParcelable("list_state", layoutManager.onSaveInstanceState())
    }

    private fun loadList() {
        if (!isLoading) {
            isLoading = true
            swipe_refresh.isRefreshing = true
            GlobalScope.launch(Dispatchers.Main + parentJob) {
                val liveMsgs = LiveApiImpl.getLiveMessages(newsId).await()
                if (liveMsgs != null) {
                    if (liveMsgs.isNotEmpty()) {
                        liveMessages.clear()
                        liveMessages.addAll(liveMsgs)
                        adapter.notifyDataSetChanged()
                    } else {
                        list_view.setAllContentLoaded(true)
                        ToastUtil.showToast(R.string.no_more_content)
                    }
                } else {
                    ToastUtil.showToast(R.string.timeout_no_internet)
                }
                UiUtil.switchVisibility(list_view, error_placeholder, liveMessages.size)
                isLoading = false
                swipe_refresh.isRefreshing = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.live_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_live_info -> {
                val intent = Intent(this, ArticleActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("live_info", "")
                startActivity(intent)
            }
        }
        return true
    }
}
