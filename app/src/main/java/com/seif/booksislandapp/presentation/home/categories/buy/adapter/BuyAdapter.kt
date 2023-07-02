package com.seif.booksislandapp.presentation.home.categories.buy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.BuyDonateAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate
import com.seif.booksislandapp.utils.setBookUriImage

class BuyAdapter : RecyclerView.Adapter<BuyAdapter.MyViewHolder>() {
    var onAdItemClick: OnAdItemClick<SellAdvertisement>? = null
    var buyAds: List<SellAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: BuyDonateAdvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(buyAdvertisement: SellAdvertisement, position: Int) {
            binding.tvTitle.text = buyAdvertisement.book.title
            binding.tvDescription.text = buyAdvertisement.book.description
            binding.tvPublishDate.text = buyAdvertisement.publishDate.formatDate()
            binding.tvPrice.text = itemView.context.getString(R.string.egypt_pound, buyAdvertisement.price)
            binding.tvLocation.text = buyAdvertisement.location
            binding.ivImage.setBookUriImage(buyAdvertisement.book.images.first())
            binding.cvBuyDonateAd.setOnClickListener {
                onAdItemClick?.onAdItemClick(buyAdvertisement, position)
            }
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
        holder.bind(buyAds[position], position)
    }

    override fun getItemCount(): Int {
        return buyAds.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newBuyAds: List<SellAdvertisement>) {
        this.buyAds = newBuyAds
        notifyDataSetChanged()
    }
}