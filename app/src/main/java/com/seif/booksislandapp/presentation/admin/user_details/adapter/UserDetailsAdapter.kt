package com.seif.booksislandapp.presentation.admin.user_details.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.databinding.UserDetailItemBinding
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick

class UserDetailsAdapter : RecyclerView.Adapter<UserDetailsAdapter.MyViewHolder>() {

    var onAdItemClick: OnAdItemClick<Report>? = null
    var reports: List<Report> = emptyList()
    var reportNum = 0
    inner class MyViewHolder(private val binding: UserDetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report, position: Int) {
            binding.tvReported.text = report.reporterName
            binding.tvCategoryType.text = report.category
            reportNum = position + 1
            binding.tvReportNumber.text = reportNum.toString()
            binding.cvAdminReports.setOnClickListener {
                onAdItemClick?.onAdItemClick(report, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            UserDetailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(reports[position], position)
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newReports: List<Report>) {
        this.reports = newReports
        notifyDataSetChanged()
    }
}