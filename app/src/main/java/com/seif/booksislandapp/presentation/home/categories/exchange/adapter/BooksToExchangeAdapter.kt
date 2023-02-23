package com.seif.booksislandapp.presentation.home.categories.exchange.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.databinding.ExchangeListItemBinding
import com.seif.booksislandapp.domain.model.book.BooksToExchange

class BooksToExchangeAdapter : RecyclerView.Adapter<BooksToExchangeAdapter.MyViewHolder>() {
    var exchangeItems: List<BooksToExchange> = emptyList()
    inner class MyViewHolder(private val binding: ExchangeListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booksToExchange: BooksToExchange) {
            binding.tvBookName.text = booksToExchange.title
            binding.ivBook.load(booksToExchange.imageUrl)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ExchangeListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(exchangeItems[position])
    }

    override fun getItemCount(): Int {
        return exchangeItems.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newBooksToExchangeAds: List<BooksToExchange>) {
        this.exchangeItems = newBooksToExchangeAds
        notifyDataSetChanged()
    }
}