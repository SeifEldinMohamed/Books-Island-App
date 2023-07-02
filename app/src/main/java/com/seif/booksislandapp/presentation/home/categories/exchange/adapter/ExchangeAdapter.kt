package com.seif.booksislandapp.presentation.home.categories.exchange.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.databinding.ExchangeAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate
import com.seif.booksislandapp.utils.setBookUriImage

class ExchangeAdapter : RecyclerView.Adapter<ExchangeAdapter.MyViewHolder>() {
    var exchangeAds: List<ExchangeAdvertisement> = emptyList()
    var onAdItemClick: OnAdItemClick<ExchangeAdvertisement>? = null

    inner class MyViewHolder(private val binding: ExchangeAdvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val booksToExchangeAdapter by lazy { BooksToExchangeAdapter() }
        fun bind(exchangeAdvertisement: ExchangeAdvertisement, position: Int) {
            binding.tvBookName.text = exchangeAdvertisement.book.title
            binding.tvDiscription.text = exchangeAdvertisement.book.description
            binding.tvPublishDate.text = exchangeAdvertisement.publishDate.formatDate()
            binding.rvContent.adapter = booksToExchangeAdapter
            booksToExchangeAdapter.updateList(exchangeAdvertisement.booksToExchange)
            binding.tvLocation.text = exchangeAdvertisement.location
            binding.ivBook.setBookUriImage(exchangeAdvertisement.book.images.first())
            binding.cvExchange.setOnClickListener {
                onAdItemClick?.onAdItemClick(exchangeAdvertisement, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ExchangeAdvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(exchangeAds[position], position)
    }

    override fun getItemCount(): Int {
        return exchangeAds.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newExchangeAds: List<ExchangeAdvertisement>) {
        this.exchangeAds = newExchangeAds
        notifyDataSetChanged()
    }
}