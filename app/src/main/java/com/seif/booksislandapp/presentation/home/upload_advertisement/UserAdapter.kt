package com.seif.booksislandapp.presentation.home.upload_advertisement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.databinding.UserItemBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.MyDiffUtil

class UserAdapter : RecyclerView.Adapter<UserAdapter.MyViewHolder>() {
    var onAdItemClick: OnAdItemClick<User>? = null
    private var users: List<User> = emptyList()

    inner class MyViewHolder(private val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, position: Int) {
            binding.ivAvatarImage.load(user.avatarImage) {
                crossfade(200)
            }
            binding.tvUsername.text = user.username
            binding.cvMyChat.setOnClickListener {
                onAdItemClick?.onAdItemClick(user, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            UserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun updateList(newUsers: List<User>) {
        val diffUtilCallBack = MyDiffUtil(this.users, newUsers)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtilCallBack)
        this.users = newUsers
        diffUtilResult.dispatchUpdatesTo(this)
    }
}