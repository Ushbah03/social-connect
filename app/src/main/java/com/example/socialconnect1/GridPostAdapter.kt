package com.example.socialconnect1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.bumptech.glide.Glide

class GridPostAdapter(
    private val posts: List<Post>,
    private val onPostClick: (Post) -> Unit
) : Adapter<GridPostAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.gridImage)

        init {
            itemView.setOnClickListener {
                onPostClick(posts[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        Glide.with(holder.itemView.context)
            .load(post.image_url)
            .placeholder(R.drawable.post_placeholder)
            .centerCrop()
            .into(holder.image)
    }

    override fun getItemCount(): Int = posts.size
}
