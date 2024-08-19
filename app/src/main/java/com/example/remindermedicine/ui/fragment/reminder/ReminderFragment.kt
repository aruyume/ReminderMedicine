package com.example.remindermedicine.ui.fragment.reminder

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.remindermedicine.R
import com.example.remindermedicine.data.db.daos.Reminder
import com.example.remindermedicine.databinding.FragmentReminderBinding
import com.example.remindermedicine.databinding.ReminderDialogBinding
import com.example.remindermedicine.utils.ReminderWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReminderFragment : Fragment(R.layout.fragment_reminder) {

    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReminderViewModel by viewModel()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showAddReminderDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission required to set reminder",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReminderBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        scheduleDailyNotifications()

        binding.btnAddReminder.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(
                    requireContext()
                ).areNotificationsEnabled()
            ) {
                showNotificationPermissionDialog()
            } else {
                showAddReminderDialog()
            }
        }
    }

    private fun setupRecyclerView() {
        val adapter = ReminderAdapter(viewModel) { reminder ->
            showReminderDetailsDialog(reminder)
        }
        binding.rvReminder.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun setupObservers() {
        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            (binding.rvReminder.adapter as? ReminderAdapter)?.submitList(reminders)
        }
    }

    private fun showReminderDetailsDialog(reminder: Reminder) {
        val dialogBinding = ReminderDialogBinding.inflate(layoutInflater)

        val reminderTypes = resources.getStringArray(R.array.ReminderTypes)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            reminderTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        dialogBinding.reminderType.adapter = adapter

        dialogBinding.apply {
            etTitle.setText(reminder.title)
            etInfo.setText(reminder.info)
            etAmount.setText(reminder.amount.toString())
            etMaxAmount.setText(reminder.maxAmount.toString())
            pickedDateAndTime.text = getCurrentDateAndTime(reminder.timestamp)
            reminderType.setSelection(reminderTypes.indexOf(reminder.reminderType))
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reminder Details")
            .setView(dialogBinding.root)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
            .window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showAddReminderDialog() {
        val dialogBinding = ReminderDialogBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.reminderType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.ReminderTypes)
        )

        dialogBinding.apply {
            etAmount.setText("0")
            etMaxAmount.setText("20")
            btnCancel.setOnClickListener { dialog.dismiss() }

            val pickedDate = Calendar.getInstance()

            btnSelect.setOnClickListener {
                showDateTimePicker(pickedDate) {
                    pickedDate.timeInMillis.let { timeInMillis ->
                        pickedDateAndTime.text = getCurrentDateAndTime(timeInMillis)
                    }
                }
            }

            btnSubmit.setOnClickListener {
                if (validateReminderInput(dialogBinding, pickedDate)) {
                    val title = etTitle.text.toString()
                    val info = etInfo.text.toString()
                    val reminderType =
                        resources.getStringArray(R.array.ReminderTypes)[reminderType.selectedItemPosition]
                    val maxAmount = etMaxAmount.text.toString().toIntOrNull() ?: 20

                    val timeDelayInSeconds =
                        (pickedDate.timeInMillis / 1000L) - (Calendar.getInstance().timeInMillis / 1000L)
                    createWorkRequest(title, reminderType, timeDelayInSeconds)

                    viewModel.addReminder(
                        title,
                        info,
                        pickedDate.timeInMillis,
                        maxAmount,
                        reminderType
                    )
                    Toast.makeText(requireContext(), "Reminder Added", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun showDateTimePicker(pickedDate: Calendar, onDateTimeSelected: () -> Unit) {
        val year = pickedDate.get(Calendar.YEAR)
        val month = pickedDate.get(Calendar.MONTH)
        val day = pickedDate.get(Calendar.DAY_OF_MONTH)
        val hour = pickedDate.get(Calendar.HOUR_OF_DAY)
        val minute = pickedDate.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                pickedDate.set(
                    selectedYear,
                    selectedMonth,
                    selectedDay,
                    selectedHour,
                    selectedMinute
                )
                onDateTimeSelected()
            }, hour, minute, false).show()
        }, year, month, day).show()
    }

    private fun validateReminderInput(
        dialogBinding: ReminderDialogBinding,
        pickedDate: Calendar
    ): Boolean {
        return when {
            dialogBinding.etTitle.text.isNullOrEmpty() -> {
                dialogBinding.etTitle.error = "Please enter title"
                false
            }

            dialogBinding.etInfo.text.isNullOrEmpty() -> {
                dialogBinding.etInfo.error = "Please enter info"
                false
            }

            dialogBinding.pickedDateAndTime.text == resources.getString(R.string.date_and_time) -> {
                dialogBinding.pickedDateAndTime.error = "Please select date and time"
                false
            }

            (pickedDate.timeInMillis / 1000L) - (Calendar.getInstance().timeInMillis / 1000L) < 0 -> {
                Toast.makeText(
                    requireContext(),
                    "Can't set reminders for past date",
                    Toast.LENGTH_LONG
                ).show()
                false
            }

            else -> true
        }
    }

    private fun scheduleDailyNotifications() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startOfDayDelay = calendar.timeInMillis - System.currentTimeMillis()
        val endOfDayCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endOfDayDelay = endOfDayCalendar.timeInMillis - System.currentTimeMillis()

        createWorkRequest("Daily Reminder", "Check medicines to take today", startOfDayDelay)

        createWorkRequest("Daily Report", "Daily report of medicines taken", endOfDayDelay)
    }

    private fun createWorkRequest(title: String, message: String, delay: Long) {
        val reminderWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "Title" to title,
                    "Message" to message
                )
            )
            .build()
        WorkManager.getInstance(requireContext()).enqueue(reminderWorkRequest)
    }

    private fun getCurrentDateAndTime(millis: Long): String {
        return SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date(millis))
    }

    private fun showNotificationPermissionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("Notification permission required to show reminder notifications")
            .setPositiveButton("OK") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}