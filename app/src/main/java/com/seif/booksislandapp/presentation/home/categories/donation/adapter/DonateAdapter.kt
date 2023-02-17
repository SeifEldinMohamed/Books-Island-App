package com.seif.booksislandapp.presentation.home.categories.donation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.BuyDonateAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.utils.formatDate

class DonateAdapter : RecyclerView.Adapter<DonateAdapter.MyViewHolder>() {
    var donateAds: List<DonateAdvertisement> = emptyList()
    inner class MyViewHolder(private val binding: BuyDonateAdvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(donateAdvertisement: DonateAdvertisement) {
            binding.tvTitle.text = donateAdvertisement.book.title
            binding.tvDescription.text = donateAdvertisement.book.description
            binding.tvDate.text = donateAdvertisement.publishTime.formatDate()
            binding.tvPrice.text = itemView.context.getString(R.string.free)

            binding.tvLocation.text = donateAdvertisement.location
            binding.ivImage.load(donateAdvertisement.book.images.first())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            BuyDonateAdvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(donateAds[position])
    }

    override fun getItemCount(): Int {
        return donateAds.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newDonateAds: List<DonateAdvertisement>) {
        this.donateAds = newDonateAds
        notifyDataSetChanged()
    }
}