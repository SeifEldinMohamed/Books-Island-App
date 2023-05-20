package com.seif.booksislandapp.presentation.home.chat_room.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.chat.Message
import timber.log.Timber

class ChatRoomAdapter : RecyclerView.Adapter<ChatRoomAdapter.ChatBubbleViewHolder>() {
    private var messages: List<Message> = emptyList()
    private var currentUserId: String = ""

    class ChatBubbleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text_view)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)

        fun bind(message: Message) {
            Timber.d("bind: ${message.imageUrl}")
            if (message.imageUrl != null) {
                messageTextView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                imageView.load(message.imageUrl)
            } else { // text message
                messageTextView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
                messageTextView.text = message.text
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatBubbleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val layoutResId = if (viewType == VIEW_TYPE_SENDER) {
            R.layout.sender_bubble_layout
        } else {
            R.layout.receiver_bubble_layout
        }
        val view = layoutInflater.inflate(layoutResId, parent, false)
        return ChatBubbleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatBubbleViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENDER
        } else {
            VIEW_TYPE_RECEIVER
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun addMessages(newMessages: List<Message>, currentUserId: String) {
        Timber.d("addMessages: $newMessages")
        this.currentUserId = currentUserId
        val diffUtilCallBack = MessagesDiffUtil(this.messages, newMessages)
        val result = DiffUtil.calculateDiff(diffUtilCallBack)
        this.messages = newMessages
        result.dispatchUpdatesTo(this)
    }

    companion object {
        private const val VIEW_TYPE_SENDER = 0
        private const val VIEW_TYPE_RECEIVER = 1
    }
}