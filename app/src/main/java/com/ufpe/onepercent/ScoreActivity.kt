package com.ufpe.onepercent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ufpe.onepercent.data.UserListAdapter
import com.ufpe.onepercent.model.User
import kotlinx.android.synthetic.main.activity_score.*

class ScoreActivity : AppCompatActivity() {

    private var adapter: UserListAdapter? = null
    private var userList: ArrayList<User>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        setTitle("1% - Scoreboard")



        // populate userList
        var data = intent.extras
        if (data != null) {
            var listType = object: TypeToken<ArrayList<User>>() { }.type
            userList = Gson().fromJson<ArrayList<User>>(data.getString("userList"), listType)
        }

        layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(userList!!, this)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        adapter!!.notifyDataSetChanged()
    }
}
