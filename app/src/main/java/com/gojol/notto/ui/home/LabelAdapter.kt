package com.gojol.notto.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gojol.notto.databinding.ItemLabelBinding

class LabelAdapter(private val allChipCheckedCallback: () -> (Unit)) :
    ListAdapter<LabelWithCheck, LabelAdapter.LabelViewHolder>(LabelDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        return LabelViewHolder(
            ItemLabelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE
    }

    fun moveItem(from: Int, to: Int, isChecked: Boolean) {
        var newList = currentList.toMutableList()
        if (newList[0].isChecked){
            val header = currentList[0].copy(isChecked = false)
            newList = newList.filter { it.label.order != 0 }.toMutableList()
            newList.add(0, header)
        }

        val currentLabel = newList.removeAt(from).copy(isChecked = isChecked)
        newList.add(to, currentLabel)

        submitList(newList)
    }

    fun allChipChecked() {
        val header = currentList[0].copy(isChecked = true)
        val newList = currentList
            .filter { it.label.order != 0 }
            .map { it.copy(isChecked = false) }
            .sortedBy { it.label.order }
            .toMutableList()
        newList.add(0, header)

        submitList(newList)
    }

    companion object {
        const val VIEW_TYPE = 3
    }

    class LabelViewHolder(private val binding: ItemLabelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(labelWithCheck: LabelWithCheck) {
            val adapter = bindingAdapter as LabelAdapter
            binding.chipHomeLabel.isChecked = labelWithCheck.isChecked
            when (labelWithCheck.label.order) {
                0 -> setHeaderLabel(adapter, labelWithCheck)
                else -> setItemLabel(adapter, labelWithCheck)
            }

            binding.executePendingBindings()
        }

        private fun setHeaderLabel(adapter: LabelAdapter, labelWithCheck: LabelWithCheck) {
            with(binding) {
                item = labelWithCheck.label
                chipHomeLabel.setOnClickListener {
                    if (chipHomeLabel.isChecked) {
                        adapter.allChipCheckedCallback()
                    } else {
                        chipHomeLabel.isChecked = true
                    }
                }
            }
        }

        private fun setItemLabel(adapter: LabelAdapter, labelWithCheck: LabelWithCheck) {
            with(binding) {
                item = labelWithCheck.label
                chipHomeLabel.setOnClickListener {
                    if (chipHomeLabel.isChecked) {
                        adapter.moveItem(bindingAdapterPosition, 1, true)
                    } else {
                        val checkedList = adapter.currentList
                            .filter { it.isChecked && it != labelWithCheck }

                        val unCheckedList = adapter.currentList
                            .filter { !it.isChecked }
                            .toMutableList().apply {
                                add(labelWithCheck)
                            }.sortedBy { it.label.order }

                        if (checkedList.isEmpty()) {
                            adapter.allChipCheckedCallback()
                        } else {
                            adapter.moveItem(
                                bindingAdapterPosition,
                                checkedList.size + unCheckedList.indexOf(labelWithCheck),
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    class LabelDiff : DiffUtil.ItemCallback<LabelWithCheck>() {
        override fun areItemsTheSame(oldItem: LabelWithCheck, newItem: LabelWithCheck): Boolean {
            return oldItem.label.labelId == newItem.label.labelId
        }

        override fun areContentsTheSame(oldItem: LabelWithCheck, newItem: LabelWithCheck): Boolean {
            return oldItem == newItem
        }
    }
}
