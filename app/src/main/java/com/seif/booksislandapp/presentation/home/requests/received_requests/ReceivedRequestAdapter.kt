package com.seif.booksislandapp.presentation.home.requests.received_requests

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.ReceivedRequestItemBinding
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.utils.formatDateInDetails
import timber.log.Timber

class ReceivedRequestAdapter : RecyclerView.Adapter<ReceivedRequestAdapter.MyViewHolder>() {
    var onReceivedButtonClick: OnReceivedRequestItemClick<MyReceivedRequest>? = null
    private var myReceivedRequests: List<MyReceivedRequest> = emptyList()

    inner class MyViewHolder(private val binding: ReceivedRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(myReceivedRequest: MyReceivedRequest, position: Int) {
            binding.apply {
                tvUsername.text = myReceivedRequest.username
                ivAvatarImage.load(myReceivedRequest.avatarImage) {
                    crossfade(true)
                }
                tvDate.text = myReceivedRequest.date!!.formatDateInDetails()
                tvTitle.text = myReceivedRequest.bookTitle
                tvCondition.text = myReceivedRequest.condition
                tvCategory.text = myReceivedRequest.category
                tvType.text = myReceivedRequest.adType.toString()
                tvCondition.text = myReceivedRequest.condition
                tvEdition.text = myReceivedRequest.edition
                tvMessageReceived.text =
                    itemView.context.getString(
                        R.string.message_received,
                        myReceivedRequest.username
                    )
            }

            binding.btnAcceptRequest.setOnClickListener {
                onReceivedButtonClick?.onAcceptButtonClick(myReceivedRequest, position)
            }
            binding.btnRejectRequest.setOnClickListener {
                onReceivedButtonClick?.onRejectButtonClick(myReceivedRequest, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ReceivedRequestItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(myReceivedRequests[position], position)
    }

    override fun getItemCount(): Int {
        return myReceivedRequests.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(myReceivedRequests: List<MyReceivedRequest>) {
        Timber.d("myRequests= $myReceivedRequests")
        this.myReceivedRequests = myReceivedRequests
        notifyDataSetChanged()
    }
}