package com.mongodb.app.ui

sealed class Screen(val route: String) {
    object ReminderList : Screen("reminder_list")
    object AddOrUpdateReminder : Screen("add_update_reminder")
}
