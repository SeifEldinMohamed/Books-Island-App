package com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.MyChatItemBinding
import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDateToTime
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import timber.log.Timber

class MyChatsAdapter : RecyclerView.Adapter<MyChatsAdapter.MyViewHolder>() {
    var onAdItemClick: OnAdItemClick<MyChat>? = null
    var myChats: List<MyChat> = emptyList()

    inner class MyViewHolder(private val binding: MyChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(myChat: MyChat, position: Int) {
            binding.ivAvatarImage.load(myChat.userIChatWith.avatarImage) {
                crossfade(true)
            }
            binding.tvUsername.text = myChat.userIChatWith.username
            binding.tvTime.text = myChat.lastMessageDate!!.formatDateToTime()
            binding.tvLastMessage.text = myChat.lastMessage
            handleIsSeen(myChat, binding)

            binding.cvMyChat.setOnClickListener {
                onAdItemClick?.onAdItemClick(myChat, position)
            }
        }
    }

    private fun handleIsSeen(myChat: MyChat, binding: MyChatItemBinding) {
        if (myChat.isSeen) {
            binding.cvUnreadMessages.hide()
            binding.tvLastMessage.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.gray_medium
                )
            )
            binding.tvTime.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.gray_medium
                )
            )
        } else { // not seen
            Timber.d("adapter: ${myChat.senderId} ---> ${myChat.currentUserId}")
            if (myChat.senderId != myChat.currentUserId) { // I didn't send this message
                binding.cvUnreadMessages.show()
                binding.tvUnreadCounnter.text = myChat.unreadMessages.toString()
                binding.tvLastMessage.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_dark
                    )
                )
                binding.tvTime.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primary_dark
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            MyChatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(myChats[position], position)
    }

    override fun getItemCount(): Int {
        return myChats.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newMyChats: List<MyChat>) {
        Timber.d("updateList: myChats in Adapter = $newMyChats")
        this.myChats = newMyChats
        notifyDataSetChanged()
    }
}