package com.example.remindermedicine.ui.fragment.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remindermedicine.R
import com.example.remindermedicine.data.db.daos.Reminder
import com.example.remindermedicine.databinding.ItemReminderBinding

class ReminderAdapter(
    private val viewModel: ReminderViewModel,
    private val onClickItem: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(
    ReminderDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding =
            ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)
        holder.bind(reminder)
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) = with(binding) {
            tvTitle.text = reminder.title
            tvInfo.text = reminder.info
            tvAmount.text = "${reminder.amount}/${reminder.maxAmount}"
            tvReminderType.text = reminder.reminderType

            btnInc.setOnClickListener {
                val newAmount = reminder.amount + 1
                viewModel.updateReminderAmount(reminder, newAmount)
            }

            btnDec.setOnClickListener {
                val newAmount = (reminder.amount - 1).coerceAtLeast(0)
                viewModel.updateReminderAmount(reminder, newAmount)
            }

            root.setOnClickListener {
                onClickItem(reminder)
            }

            val imageResId = when (reminder.reminderType) {
                "before_breakfast" -> R.drawable.img_afternoon
                "after_breakfast" -> R.drawable.img_morning
                "before_lunch" -> R.drawable.img_afternoon
                "after_lunch" -> R.drawable.img_afternoon
                "before_dinner" -> R.drawable.img_night
                "after_dinner" -> R.drawable.img_night
                else -> R.drawable.img_default
            }
            imgReminder.setImageResource(imageResId)
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}