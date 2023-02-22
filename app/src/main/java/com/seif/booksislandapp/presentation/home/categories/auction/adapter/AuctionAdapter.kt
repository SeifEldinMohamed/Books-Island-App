package com.seif.booksislandapp.presentation.home.categories.auction.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.AuctionAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate

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
                    auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice }?.suggestedPrice
                        ?: auctionAdvertisement.startPrice
                    ).toString()
            )
            binding.tvLocation.text = auctionAdvertisement.location
            binding.ivImage.load(auctionAdvertisement.book.images.first()) {
                placeholder(R.drawable.book_placeholder)
            }
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newAuctionsAds: List<AuctionAdvertisement>) {
        this.auctionsAds = newAuctionsAds
        notifyDataSetChanged()
    }
}