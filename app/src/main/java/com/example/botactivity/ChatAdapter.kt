package com.example.botactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textViewMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = if (viewType == VIEW_TYPE_USER) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_message, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_bot_message, parent, false)
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.messageText.text = messages[position].text
    }

    override fun getItemCount() = messages.size
}