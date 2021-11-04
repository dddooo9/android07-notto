package com.gojol.notto.ui.home

import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gojol.notto.R
import com.gojol.notto.databinding.ItemCalendarDayBinding
import java.util.*

class CustomCalendarAdapter :
    ListAdapter<DateWithFocus, CustomCalendarAdapter.CustomViewHolder>(CalendarDiff()) {

    private var focusDate = currentList.find { it.isChecked }?.date

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE
    }

    private fun updateFocusDate(focus: DateWithFocus) {
        val newList = currentList.map { it.copy(isChecked = false) }.toMutableList()
        val focusItem = currentList.find { it.date == focus.date }?.copy(isChecked = true)
        if (focusItem != null) {
            newList[currentList.indexOf(focus)] = focusItem
        }

        submitList(newList)
    }

    companion object {
        const val VIEW_TYPE = 1
    }

    class CustomViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val date =
            listOf("일", "월", "화", "수", "목", "금", "토")

        fun bind(item: DateWithFocus) {
            val adapter = bindingAdapter as CustomCalendarAdapter
            binding.tvDay.text = item.date

            if(item.date !in date){
                binding.tvDay.backgroundTintList =
                    ContextCompat.getColorStateList(binding.root.context, R.color.yellow_deep)
                        ?.withAlpha(51 * item.successCount)

                binding.tvDay.setOnClickListener {
                    adapter.updateFocusDate(item)
                }
            }

            if (item.date == adapter.currentList.find { it.isChecked }?.date) {
                binding.underline.visibility = View.VISIBLE

            } else if(item.date !in date) {
                binding.underline.visibility = View.INVISIBLE
            }

            binding.executePendingBindings()
        }
    }

    class CalendarDiff : DiffUtil.ItemCallback<DateWithFocus>() {
        override fun areItemsTheSame(oldItem: DateWithFocus, newItem: DateWithFocus): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DateWithFocus, newItem: DateWithFocus): Boolean {
            return oldItem == newItem
        }
    }
}
