package com.seif.booksislandapp.presentation.admin.all_users.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.AdminUserItemBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick

class AllUsersAdapter : RecyclerView.Adapter<AllUsersAdapter.MyViewHolder>() {

    var onAdItemClick: OnAdItemClick<User>? = null
    var users: List<User> = emptyList()
    inner class MyViewHolder(private val binding: AdminUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, position: Int) {
            binding.tvUsername.text = user.username
            binding.tvEmail.text = user.email
            when (user.isSuspended) {
                true -> {
                    binding.tvStatus.text = binding.root.context.getString(R.string.suspended)
                    binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.pending_orange))
                    binding.cvStatusBackground.setCardBackgroundColor(binding.root.context.getColor(R.color.light_orange))
                }
                false -> {
                    binding.tvStatus.text = binding.root.context.getString(R.string.active)
                    binding.tvStatus.setTextColor(binding.root.context.getColor(R.color.accepted_green))
                    binding.cvStatusBackground.setCardBackgroundColor(binding.root.context.getColor(R.color.light_green))
                }
            }
            binding.tvStar.text = user.averageRate
            binding.ivAvatarImage.load(user.avatarImage) {
                placeholder(R.drawable.person_placeholder)
            }
            binding.cvUsers.setOnClickListener {
                onAdItemClick?.onAdItemClick(user, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            AdminUserItemBinding.inflate(
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }
}