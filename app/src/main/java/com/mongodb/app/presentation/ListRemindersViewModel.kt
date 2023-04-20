package com.mongodb.app.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongodb.app.data.ReminderRealmObject
import com.mongodb.app.data.ReminderRepository
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.UnaryOperator

class ListRemindersViewModel : ViewModel() {

    private val _state = mutableStateOf(ScreenState())
    val state: State<ScreenState> = _state

    private var repository = ReminderRepository.getInstance()
    val reminderListState: SnapshotStateList<ReminderRealmObject> = mutableStateListOf()

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")

    init {
        viewModelScope.launch {
            repository.getReminders()
                .collect { event: ResultsChange<ReminderRealmObject> ->
                    when (event) {
                        is InitialResults -> {
                            reminderListState.clear()
                            reminderListState.addAll(event.list.map { formatDateTime(it) })
                            _state.value = ScreenState(loading = false)
                        }
                        is UpdatedResults -> {
                            if (event.deletions.isNotEmpty() && reminderListState.isNotEmpty()) {
                                event.deletions.reversed().forEach {
                                    reminderListState.removeAt(it)
                                }
                            }
                            if (event.insertions.isNotEmpty()) {
                                event.insertions.forEach {
                                    reminderListState.add(it, formatDateTime(event.list[it]))
                                }
                            }
                            if (event.changes.isNotEmpty()) {
                                event.changes.forEach {
                                    reminderListState[it] = formatDateTime(event.list[it])
                                }
                            }
                        }
                        else -> Unit // No-op
                    }
                }
        }
    }

    private fun formatDateTime(reminder: ReminderRealmObject): ReminderRealmObject {
        reminder.dateTimeText = Instant.ofEpochMilli(reminder.dateTime)
            .atZone(ZoneId.systemDefault()).format(formatter)
        return reminder
    }

    fun deleteReminder(reminder: ReminderRealmObject) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    data class ScreenState(
        val loading: Boolean = true,
    )
}