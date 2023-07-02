package com.seif.booksislandapp.presentation.home.ad_details.sell.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.RelatedAdsItemBinding
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate
import com.seif.booksislandapp.utils.setBookUriImage

class RelatedSellAdsAdapter : RecyclerView.Adapter<RelatedSellAdsAdapter.MyViewHolder>() {
    var onRelatedAdItemClick: OnAdItemClick<SellAdvertisement>? = null
    private var relatedBuyAds: List<SellAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: RelatedAdsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(buyAdvertisement: SellAdvertisement, position: Int) {
            binding.tvTitle.text = buyAdvertisement.book.title
            binding.tvPublishDate.text = buyAdvertisement.publishDate.formatDate()
            binding.tvPrice.text =
                itemView.context.getString(R.string.egypt_pound, buyAdvertisement.price)
            binding.tvLocation.text = buyAdvertisement.location
            binding.ivBook.setBookUriImage(buyAdvertisement.book.images.first())

            binding.cvRelatedAd.setOnClickListener {
                onRelatedAdItemClick?.onAdItemClick(buyAdvertisement, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            RelatedAdsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(relatedBuyAds[position], position)
    }

    override fun getItemCount(): Int {
        return relatedBuyAds.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newBuyAds: List<SellAdvertisement>) {
        this.relatedBuyAds = newBuyAds
        notifyDataSetChanged()
    }
}