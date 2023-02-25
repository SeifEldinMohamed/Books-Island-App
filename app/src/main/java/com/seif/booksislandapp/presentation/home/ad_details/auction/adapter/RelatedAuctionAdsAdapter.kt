package com.seif.booksislandapp.presentation.home.ad_details.auction.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.RelatedAuctionAdsItemBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate

class RelatedAuctionAdsAdapter : RecyclerView.Adapter<RelatedAuctionAdsAdapter.MyViewHolder>() {
    var onRelatedAdItemClick: OnAdItemClick<AuctionAdvertisement>? = null
    private var relatedAuctionAds: List<AuctionAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: RelatedAuctionAdsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(auctionAdvertisement: AuctionAdvertisement, position: Int) {
            binding.tvTitle.text = auctionAdvertisement.book.title
            binding.tvPublishDate.text = auctionAdvertisement.publishDate.formatDate()
            binding.tvCurrentPrice.text = itemView.context.getString(
                R.string.current_price_egypt_pound,
                (
                        auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice }?.suggestedPrice
                            ?: auctionAdvertisement.startPrice?.toInt()
                        ).toString()
            )
            binding.tvLocation.text = auctionAdvertisement.location
            binding.ivBook.load(auctionAdvertisement.book.images.first())

            binding.cvRelatedAd.setOnClickListener {
                onRelatedAdItemClick?.onAdItemClick(auctionAdvertisement, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            RelatedAuctionAdsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(relatedAuctionAds[position], position)
    }

    override fun getItemCount(): Int {
        return relatedAuctionAds.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newBuyAds: List<AuctionAdvertisement>) {
        this.relatedAuctionAds = newBuyAds
        notifyDataSetChanged()
    }
}