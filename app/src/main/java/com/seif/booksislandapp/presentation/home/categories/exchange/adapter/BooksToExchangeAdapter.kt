package com.seif.booksislandapp.presentation.home.categories.exchange.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.ExchangeListItemBinding
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show

class BooksToExchangeAdapter : RecyclerView.Adapter<BooksToExchangeAdapter.MyViewHolder>() {
    var exchangeItems: List<BooksToExchange> = emptyList()
    inner class MyViewHolder(private val binding: ExchangeListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booksToExchange: BooksToExchange, position: Int) {
            binding.tvBookName.text = booksToExchange.title
            binding.ivBook.load(booksToExchange.imageUri)
            binding.tvBookAuther.text = itemView.context.getString(R.string.by, booksToExchange.author)
            if (position == exchangeItems.size - 1)
                binding.line.hide()
            else
                binding.line.show()
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
        holder.bind(exchangeItems[position], position)
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