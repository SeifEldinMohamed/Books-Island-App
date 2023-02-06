package com.seif.booksislandapp.presentation.home.categories.book_categories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devs.vectorchildfinder.VectorChildFinder
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.CategoryItemBinding
import com.seif.booksislandapp.domain.model.book.BookCategory

class BookCategoriesAdapter : ListAdapter<BookCategory, BookCategoriesAdapter.MyViewHolder>(
    diffCallBack
) {
    companion object {
        val diffCallBack = object : DiffUtil.ItemCallback<BookCategory>() {
            override fun areItemsTheSame(oldItem: BookCategory, newItem: BookCategory): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: BookCategory, newItem: BookCategory): Boolean {
                return oldItem == newItem
            }
        }
    }
    var onCategoryItemClick: OnCategoryItemClick<BookCategory>? = null

    inner class MyViewHolder(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookCategory) {
            binding.tvName.text = item.name
            val vector = VectorChildFinder(binding.root.context, R.drawable.ic_book_category, binding.ivBookCategory)
            vector.findPathByName("book").fillColor = ContextCompat.getColor(binding.root.context, item.color)
            binding.cvBookCategory.setOnClickListener {
                onCategoryItemClick?.onCategoryItemClick(bookCategory = item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}