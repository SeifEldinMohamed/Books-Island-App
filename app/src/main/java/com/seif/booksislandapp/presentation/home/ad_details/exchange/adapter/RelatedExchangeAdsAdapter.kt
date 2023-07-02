package com.seif.booksislandapp.presentation.home.ad_details.exchange.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.RelatedAdsItemBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate
import com.seif.booksislandapp.utils.setBookUriImage

class RelatedExchangeAdsAdapter : RecyclerView.Adapter<RelatedExchangeAdsAdapter.MyViewHolder>() {
    var onRelatedAdItemClick: OnAdItemClick<ExchangeAdvertisement>? = null
    private var relatedExchangeAds: List<ExchangeAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: RelatedAdsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exchangeAdvertisement: ExchangeAdvertisement, position: Int) {
            binding.tvTitle.text = exchangeAdvertisement.book.title
            binding.tvPublishDate.text = exchangeAdvertisement.publishDate.formatDate()
            binding.tvPrice.text = itemView.context.getString(R.string.exchange)
            binding.tvLocation.text = exchangeAdvertisement.location
            binding.ivBook.setBookUriImage(exchangeAdvertisement.book.images.first())

            binding.cvRelatedAd.setOnClickListener {
                onRelatedAdItemClick?.onAdItemClick(exchangeAdvertisement, position)
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
        holder.bind(relatedExchangeAds[position], position)
    }

    override fun getItemCount(): Int {
        return relatedExchangeAds.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newExchangeAds: List<ExchangeAdvertisement>) {
        this.relatedExchangeAds = newExchangeAds
        notifyDataSetChanged()
    }
}