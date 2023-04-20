package com.mongodb.app.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mongodb.app.data.ReminderRealmObject
import com.mongodb.app.ui.Screen
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ReminderListScreen(
    navController: NavController,
    viewModel: ListRemindersViewModel = viewModel(),
) {

    val reminders: List<ReminderRealmObject> = viewModel.reminderListState
    ReminderListContent(
        reminders = reminders,
        state = viewModel.state,
        goBack = { navController.navigate("${Screen.AddOrUpdateReminder.route}/${false}") },
        onEdit = {
            AddReminderViewModel.SELECTED_KEY = it._id
            navController.navigate("${Screen.AddOrUpdateReminder.route}/${true}")
        },
        onDelete = viewModel::deleteReminder
    )
}

@Composable
fun ReminderListContent(
    reminders: List<ReminderRealmObject>,
    state: State<ListRemindersViewModel.ScreenState>,
    goBack: () -> Unit,
    onEdit: (ReminderRealmObject) -> Unit,
    onDelete: (ReminderRealmObject) -> Unit,
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "My Reminders") })
    }, content = {

        if (state.value.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it), contentAlignment = Alignment.Center
            ) {
                Text(text = "No data")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                items(reminders) { reminder ->
                    ReminderItem(reminder, onEdit, onDelete)
                    Divider()
                }
            }
        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = goBack) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    })
}

@Composable
fun ReminderItem(
    reminder: ReminderRealmObject,
    onEdit: (ReminderRealmObject) -> Unit,
    onDelete: (ReminderRealmObject) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = reminder.title, style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = reminder.description)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Date: ${reminder.dateTimeText}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Repeat: ${repeatOptions[reminder.repeatInterval]}")
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            IconButton(onClick = { onEdit(reminder) }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { onDelete(reminder) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}