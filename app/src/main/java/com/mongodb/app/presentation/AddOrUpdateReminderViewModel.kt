package com.mongodb.app.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongodb.app.ARG_IS_UPDATE
import com.mongodb.app.data.ReminderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AddReminderViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        var SELECTED_KEY: ObjectId = BsonObjectId()
    }

    // State to hold the entire state of the screen
    private val _state = mutableStateOf(ScreenState())
    val state: State<ScreenState> = _state

    // SharedFlow to emit validation events
    private val _events = MutableSharedFlow<Events>()
    val events: SharedFlow<Events> = _events

    private val repository = ReminderRepository.getInstance()

    private val isUpdate: Boolean = savedStateHandle[ARG_IS_UPDATE]!!

    init {
        if (!isUpdate) {
            _state.value = ScreenState(initialLoading = false)
        } else {
            viewModelScope.launch {
                val reminder = repository.getReminder(SELECTED_KEY)
                reminder?.let {
                    val dateTime =
                        Instant.ofEpochMilli(reminder.dateTime).atZone(ZoneId.systemDefault())
                    _state.value = ScreenState(
                        initialLoading = false,
                        title = reminder.title,
                        description = reminder.description,
                        selectedDate = dateTime.toLocalDate(),
                        selectedTime = dateTime.toLocalTime(),
                        selectedRepeatOption = reminder.repeatInterval,
                    )
                }
            }
        }
    }

    fun setTile(text: String) {
        _state.value = _state.value.copy(title = text)
    }

    fun setDescription(text: String) {
        _state.value = _state.value.copy(description = text)
    }

    fun setSelectedDate(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date)
    }

    fun setSelectedTime(time: LocalTime) {
        _state.value = _state.value.copy(selectedTime = time)
    }

    fun setShowDatePicker(show: Boolean) {
        _state.value = _state.value.copy(showDatePicker = show)
    }

    fun setShowTimePicker(show: Boolean) {
        _state.value = _state.value.copy(showTimePicker = show)
    }

    fun setSelectedRepeatOption(option: Int) {
        _state.value = _state.value.copy(selectedRepeatOption = option)
    }

    fun setShowRepeatOptions(show: Boolean) {
        _state.value = _state.value.copy(showRepeatOptions = show)
    }

    fun saveReminder() {
        // Validate the data
        if (state.value.title.isEmpty()) {
            // Emit a validation error event if the title is empty
            viewModelScope.launch {
                _events.emit(Events.TitleEmptyError)
            }
            return
        }
        if (state.value.description.isEmpty()) {
            // Emit a validation error event if the description is empty
            viewModelScope.launch {
                _events.emit(Events.DescriptionEmptyError)
            }
            return
        }

        // Save the data to the database
        val reminder = Reminder(
            title = state.value.title,
            description = state.value.description,
            date = state.value.selectedDate,
            time = state.value.selectedTime,
            repeatInterval = state.value.selectedRepeatOption
        )

        if (isUpdate) update(reminder)
        else insert(reminder)
    }

    private fun insert(reminder: Reminder) {
        viewModelScope.launch {
            repository.addReminder(reminder)
            _events.emit(Events.InsertSuccess)
        }
    }

    private fun update(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(SELECTED_KEY, reminder)
            _events.emit(Events.UpdateSuccess)
        }
    }

    data class ScreenState(
        val initialLoading: Boolean = true,
        val title: String = "",
        val description: String = "",
        val selectedDate: LocalDate = LocalDate.now(),
        val selectedTime: LocalTime = LocalTime.now(),
        val showDatePicker: Boolean = false,
        val showTimePicker: Boolean = false,
        val selectedRepeatOption: Int = 0,
        val showRepeatOptions: Boolean = false
    )

    sealed class Events {
        object TitleEmptyError : Events()
        object DescriptionEmptyError : Events()
        object InsertSuccess : Events()
        object UpdateSuccess : Events()
    }
}

data class Reminder(
    val title: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime,
    val repeatInterval: Int,
    val timeString: String = ""
)