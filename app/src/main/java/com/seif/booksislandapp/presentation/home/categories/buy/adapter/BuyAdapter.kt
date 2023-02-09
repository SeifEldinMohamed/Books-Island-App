package com.seif.booksislandapp.presentation.home.categories.buy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.BuyDonateAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.utils.formatDate

class BuyAdapter : RecyclerView.Adapter<BuyAdapter.MyViewHolder>() {
    // var onImageItemClick: OnImageItemClick<Uri>? = null
    var buyAds: List<SellAdvertisement> = emptyList()

    inner class MyViewHolder(private val binding: BuyDonateAdvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(buyAdvertisement: SellAdvertisement) {
            binding.tvTitle.text = buyAdvertisement.book.title
            binding.tvDescription.text = buyAdvertisement.book.description
            binding.tvDate.text = buyAdvertisement.publishTime.formatDate()
            if (buyAdvertisement.price.toDouble() == 0.0) // donate
                binding.tvPrice.text = itemView.context.getString(R.string.free)
            else // price
                binding.tvPrice.text = itemView.context.getString(R.string.egypt_pound, buyAdvertisement.price)
            binding.tvLocation.text = buyAdvertisement.location
            binding.ivImage.load(buyAdvertisement.book.images.first())

            // onImageItemClick?.onRemoveImageItemClick(image, position)
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
        holder.bind(buyAds[position])
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