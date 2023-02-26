package com.seif.booksislandapp.presentation.home.categories.exchange.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.ExchangeAdvItemBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.formatDate

class ExchangeAdapter : RecyclerView.Adapter<ExchangeAdapter.MyViewHolder>() {
    var exchangeAds: List<ExchangeAdvertisement> = emptyList()
    var onAdItemClick: OnAdItemClick<ExchangeAdvertisement>? = null
    private val booksToExchangeAdapter by lazy { BooksToExchangeAdapter() }
    inner class MyViewHolder(private val binding: ExchangeAdvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(exchangeAdvertisement: ExchangeAdvertisement, position: Int) {
            binding.tvBookName.text = exchangeAdvertisement.book.title
            binding.tvDiscription.text = exchangeAdvertisement.book.description
            binding.tvPublishDate.text = exchangeAdvertisement.publishDate.formatDate()

            booksToExchangeAdapter.updateList(exchangeAdvertisement.booksToExchange)
            binding.rvContent.adapter = booksToExchangeAdapter

            binding.tvLocation.text = exchangeAdvertisement.location
            binding.ivBook.load(exchangeAdvertisement.book.images.first()) {
                placeholder(R.drawable.book_placeholder)
            }
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