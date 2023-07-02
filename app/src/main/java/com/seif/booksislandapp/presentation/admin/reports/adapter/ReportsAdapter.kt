package com.seif.booksislandapp.presentation.admin.reports.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seif.booksislandapp.databinding.ReportItemBinding
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.presentation.admin.OnReportReviewedItemClick
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import java.text.SimpleDateFormat

class ReportsAdapter : RecyclerView.Adapter<ReportsAdapter.MyViewHolder>() {

    var onAdItemClick: OnAdItemClick<Report>? = null
    var onReportReviewedItemClick: OnReportReviewedItemClick<Report>? = null
    var reports: List<Report> = emptyList()
    var reportNum = 0

    inner class MyViewHolder(private val binding: ReportItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        private val formatter = SimpleDateFormat("dd MMM yyyy")
        fun bind(report: Report, position: Int) {
            binding.tvReported.text = report.reporterName
            binding.tvReportedToName.text = report.reportedPersonName
            binding.tvReportDate.text = report.date?.let { formatter.format(it) }
            binding.tvCategoryType.text = report.category
            reportNum = position + 1
            binding.tvReportNumber.text = reportNum.toString()
            binding.cvAdminReports.setOnClickListener {
                onAdItemClick?.onAdItemClick(report, position)
            }
            binding.ivCancelReport.setOnClickListener {
                onReportReviewedItemClick?.onReportReviewedItemClick(report)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ReportItemBinding.inflate(
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