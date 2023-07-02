package com.seif.booksislandapp.presentation.home.requests.sent_requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.SentRequestItemBinding
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.MyDiffUtil
import com.seif.booksislandapp.utils.formatDateInDetails

class SentRequestAdapter : RecyclerView.Adapter<SentRequestAdapter.MyViewHolder>() {
    var onCancelButtonItemClick: OnAdItemClick<MySentRequest>? = null
    private var mySentRequests: List<MySentRequest> = emptyList()

    inner class MyViewHolder(private val binding: SentRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mySentRequest: MySentRequest, position: Int) {
            binding.apply {
                tvUsername.text = mySentRequest.username
                ivAvatarImage.load(mySentRequest.avatarImage) {
                    crossfade(true)
                }
                tvDate.text = mySentRequest.date!!.formatDateInDetails()
                tvTitle.text = mySentRequest.bookTitle
                tvCondition.text = mySentRequest.condition
                tvCategory.text = mySentRequest.category
                tvType.text = mySentRequest.adType.toString()
                tvCondition.text = mySentRequest.condition
                tvEdition.text = mySentRequest.edition
                tvMessageSent.text =
                    itemView.context.getString(R.string.message_sent, mySentRequest.username)
                tvStatus.text = mySentRequest.status
            }
            handleRequestStatusStyle(binding, mySentRequest.status)

            binding.btnCancelRequest.setOnClickListener {
                onCancelButtonItemClick?.onAdItemClick(mySentRequest, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            SentRequestItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mySentRequests[position], position)
    }

    override fun getItemCount(): Int {
        return mySentRequests.size
    }

    private fun handleRequestStatusStyle(binding: SentRequestItemBinding, status: String) {
        when (status) {
            "Pending" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.pending_orange
                    )
                )
                binding.cvStatusBackground.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.light_orange
                    )
                )
                binding.btnCancelRequest.text = binding.root.context.getString(R.string.cancel)
            }
            "Accepted" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.accepted_green
                    )
                )
                binding.cvStatusBackground.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.light_green
                    )
                )
                binding.btnCancelRequest.text = binding.root.context.getString(R.string.delete)
            }
            "Rejected" -> {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.rejected_red
                    )
                )
                binding.cvStatusBackground.setCardBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.light_red
                    )
                )
                binding.btnCancelRequest.text = binding.root.context.getString(R.string.delete)
            }
        }
    }

    fun updateList(mySentRequests: List<MySentRequest>) {
        val diffUtilCallBack = MyDiffUtil(this.mySentRequests, mySentRequests)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtilCallBack)
        this.mySentRequests = mySentRequests
        diffUtilResult.dispatchUpdatesTo(this)
    }
}