package com.example.socialconnect1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NotificationAdapter(
    private val notificationList: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notif = notificationList[position]

        // Show sender name
        holder.nameTextView.text = notif.senderName ?: "Someone"

        // Notification message
        val message = when (notif.type) {
            "like" -> "liked your post"
            "comment" -> "commented on your post"
            else -> "interacted with your post"
        }
        holder.messageTextView.text = message

        // Load avatar (if available)
        if (!notif.senderAvatar.isNullOrEmpty()) {
            Glide.with(holder.avatarImageView.context)
                .load(notif.senderAvatar)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(holder.avatarImageView)
        } else {
            holder.avatarImageView.setImageResource(R.drawable.profile)
        }
    }

    override fun getItemCount(): Int = notificationList.size
}
