package com.seif.booksislandapp.presentation.home.upload_advertisement.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.databinding.UploadedImageItemBinding

import timber.log.Timber

class UploadedImagesAdapter : RecyclerView.Adapter<UploadedImagesAdapter.MyViewHolder>() {
    var onImageItemClick: OnImageItemClick<Uri>? = null
    var images: List<Uri> = emptyList()
    inner class MyViewHolder(private val binding: UploadedImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Uri, position: Int) {
            binding.ivNoteImage.setImageURI(image)
            Timber.d("image uri $image")
            binding.ivRemoveImage.setOnClickListener {
                Timber.d("remove clicked")
                onImageItemClick?.onRemoveImageItemClick(image, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            UploadedImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int {
        return images.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newImages: List<Uri>) {
        this.images = newImages
        notifyDataSetChanged()
    }
}