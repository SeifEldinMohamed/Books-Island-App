package com.seif.booksislandapp.presentation.home.upload_advertisement.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.UploadedExchangeItemBinding
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import com.seif.booksislandapp.utils.setBookUriImage
import timber.log.Timber

class UploadedBooksForExchangeAdapter : RecyclerView.Adapter<UploadedBooksForExchangeAdapter.MyViewHolder>() {
    var onImageItemClick: OnImageItemClick<Uri>? = null
    private var booksToExchangeList: List<BooksToExchange> = emptyList()
    inner class MyViewHolder(private val binding: UploadedExchangeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booksToExchange: BooksToExchange, position: Int) {
            // binding.ivNoteImage.setImageURI(booksToExchange.imageUri)
            booksToExchange.imageUri?.let {
                binding.ivNoteImage.setBookUriImage(it)
            }
            binding.tvBookName.text = booksToExchange.title
            binding.tvBookAuther.text =
                itemView.context.getString(R.string.by, booksToExchange.author)
            // Timber.d("image uri $image")
            binding.ivRemoveBook.setOnClickListener {
                Timber.d("remove clicked")
                onImageItemClick?.onRemoveImageItemClick(
                    booksToExchange.imageUri!!,
                    position,
                    "Book"
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            UploadedExchangeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(booksToExchangeList[position], position)
    }

    override fun getItemCount(): Int {
        return booksToExchangeList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newBooks: List<BooksToExchange>) {
        this.booksToExchangeList = newBooks
        notifyDataSetChanged()
    }
}