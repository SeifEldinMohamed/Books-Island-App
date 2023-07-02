package com.seif.booksislandapp.presentation.home.categories.auction.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.AuctionAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.MyDiffUtil
import com.seif.booksislandapp.utils.formatDate
import com.seif.booksislandapp.utils.setBookUriImage

class AuctionAdapter : RecyclerView.Adapter<AuctionAdapter.MyViewHolder>() {
    var onAdItemClick: OnAdItemClick<AuctionAdvertisement>? = null
    var auctionsAds: List<AuctionAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: AuctionAdvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(auctionAdvertisement: AuctionAdvertisement, position: Int) {
            binding.tvTitle.text = auctionAdvertisement.book.title
            binding.tvDescription.text = auctionAdvertisement.book.description
            binding.tvPublishDate.text = auctionAdvertisement.publishDate.formatDate()
            binding.tvCurrentPrice.text = itemView.context.getString(
                R.string.current_price_egypt_pound,
                (
                    auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice.toInt() }?.suggestedPrice
                        ?: auctionAdvertisement.startPrice?.toInt()
                    ).toString()
            )
            binding.tvLocation.text = auctionAdvertisement.location
            binding.ivImage.setBookUriImage(auctionAdvertisement.book.images.first())
            binding.tvStatus.text = auctionAdvertisement.auctionStatus.name
            binding.tvParticipant.text = auctionAdvertisement.bidders.distinctBy { it.bidderId }.size.toString()
            binding.cvAuctionAd.setOnClickListener {
                onAdItemClick?.onAdItemClick(auctionAdvertisement, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            AuctionAdvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(auctionsAds[position], position)
    }

    override fun getItemCount(): Int {
        return auctionsAds.size
    }

    fun updateList(newAuctionsAds: List<AuctionAdvertisement>) {
        val diffUtilCallBack = MyDiffUtil(this.auctionsAds, newAuctionsAds)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtilCallBack)
        this.auctionsAds = newAuctionsAds
        diffUtilResult.dispatchUpdatesTo(this)
    }
}