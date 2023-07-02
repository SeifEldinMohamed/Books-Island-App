package com.seif.booksislandapp.presentation.home.ad_details.donate.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.RelatedAdsItemBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate

class RelatedDonateAdsAdapter : RecyclerView.Adapter<RelatedDonateAdsAdapter.MyViewHolder>() {
    var onRelatedAdItemClick: OnAdItemClick<DonateAdvertisement>? = null
    private var relatedDonateAds: List<DonateAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: RelatedAdsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(donateAdvertisement: DonateAdvertisement, position: Int) {
            binding.tvTitle.text = donateAdvertisement.book.title
            binding.tvPublishDate.text = donateAdvertisement.publishDate.formatDate()
            binding.tvPrice.text = itemView.context.getString(R.string.free)
            binding.tvLocation.text = donateAdvertisement.location
            binding.ivBook.setImageURI(donateAdvertisement.book.images.first())

            binding.cvRelatedAd.setOnClickListener {
                onRelatedAdItemClick?.onAdItemClick(donateAdvertisement, position)
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
        holder.bind(relatedDonateAds[position], position)
    }

    override fun getItemCount(): Int {
        return relatedDonateAds.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newDonateAds: List<DonateAdvertisement>) {
        this.relatedDonateAds = newDonateAds
        notifyDataSetChanged()
    }
}