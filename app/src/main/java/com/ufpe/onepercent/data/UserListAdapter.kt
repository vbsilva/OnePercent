package com.ufpe.onepercent.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.ufpe.onepercent.R
import com.ufpe.onepercent.model.User

class UserListAdapter(private val list: ArrayList<User>, private val context: Context) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_row, parent, false )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindItem(list[position])
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindItem(user: User) {
            var name: TextView = itemView.findViewById(R.id.username) as TextView
            var score: TextView = itemView.findViewById(R.id.score) as TextView
            var img: ImageView = itemView.findViewById(R.id.userImg) as ImageView

            name.text = user.name
            score.text = user.score.toString()
            val url = user.photoUrl

            if(url !=null && url!= ""){
                Picasso.get().load(url).into(img)
            }

        }
    }

}