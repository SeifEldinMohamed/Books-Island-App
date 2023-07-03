package com.seif.booksislandapp.presentation.home.bidders_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.BidderItemBinding
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.MyDiffUtil

class BidderHistoryAdapter : RecyclerView.Adapter<BidderHistoryAdapter.MyViewHolder>() {
    var onChatItemClick: OnAdItemClick<String>? = null
    var bidders: List<Bidder> = emptyList()

    inner class MyViewHolder(private val binding: BidderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bidder: Bidder, position: Int) {
            binding.ivAvatarImage.load(bidder.bidderAvatar) {
                crossfade(200)
            }
            binding.tvUsername.text = bidder.bidderName
            binding.tvBidValue.text = bidder.suggestedPrice
            handleFirstItem(binding, position)
            binding.btnChat.setOnClickListener {
                onChatItemClick?.onAdItemClick(bidder.bidderId, position)
            }
        }
    }

    private fun handleFirstItem(binding: BidderItemBinding, position: Int) {
        if (position == 0) { // first item
            binding.tvBidValue.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.primary
                )
            )
            binding.cvBidder.strokeColor =
                ContextCompat.getColor(binding.root.context, R.color.primary)
        } else {
            binding.tvBidValue.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.gray_medium
                )
            )
            binding.cvBidder.strokeColor =
                ContextCompat.getColor(binding.root.context, R.color.gray_medium)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            BidderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(bidders[position], position)
    }

    override fun getItemCount(): Int {
        return bidders.size
    }

    fun updateList(newBidders: List<Bidder>) {
        val diffUtilCallBack = MyDiffUtil(this.bidders, newBidders)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtilCallBack)
        this.bidders = newBidders
        diffUtilResult.dispatchUpdatesTo(this)
    }
}