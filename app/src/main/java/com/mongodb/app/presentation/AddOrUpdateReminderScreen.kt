package com.mongodb.app.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

val repeatOptions = listOf("Does not repeat", "Daily", "Weekly", "Monthly", "Yearly")

@Composable
fun AddOrUpdateReminderScreen(
    navController: NavController,
    isUpdateScreen: Boolean,
    viewModel: AddReminderViewModel = viewModel(),
) {

    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    LaunchedEffect(key1 = true, block = {
        viewModel.events.collectLatest {
            when (it) {
                AddReminderViewModel.Events.DescriptionEmptyError -> {
                    scaffoldState.snackbarHostState.showSnackbar("Description cannot be empty")
                }
                AddReminderViewModel.Events.InsertSuccess -> {
                    Toast.makeText(context, "Reminder saved successfully", Toast.LENGTH_SHORT)
                        .show()
                    navController.popBackStack()
                }
                AddReminderViewModel.Events.TitleEmptyError -> {
                    scaffoldState.snackbarHostState.showSnackbar("Title cannot be empty")
                }
                AddReminderViewModel.Events.UpdateSuccess -> {
                    Toast.makeText(context, "Reminder updated successfully", Toast.LENGTH_SHORT)
                        .show()
                    navController.popBackStack()
                }
            }
        }
    })

    val state: AddReminderViewModel.ScreenState = viewModel.state.value
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = if (isUpdateScreen) "Update Reminder" else "Add Reminder")
            },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
        AddOrUpdateReminderScreenContent(
            modifier = Modifier.padding(it),
            state = state,
            onTitleChange = viewModel::setTile,
            onDescriptionChange = viewModel::setDescription,
            showDatePickerDialog = viewModel::setShowDatePicker,
            showTimePickerDialog = viewModel::setShowTimePicker,
            timeSelected = viewModel::setSelectedTime,
            dateSelected = viewModel::setSelectedDate,
            showRepeatOption = viewModel::setShowRepeatOptions,
            repeatSelected = viewModel::setSelectedRepeatOption,
            addReminder = viewModel::saveReminder
        )
    }
}

@Composable
fun AddOrUpdateReminderScreenContent(
    modifier: Modifier,
    state: AddReminderViewModel.ScreenState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    showDatePickerDialog: (Boolean) -> Unit,
    showTimePickerDialog: (Boolean) -> Unit,
    timeSelected: (LocalTime) -> Unit,
    dateSelected: (LocalDate) -> Unit,
    showRepeatOption: (Boolean) -> Unit,
    repeatSelected: (Int) -> Unit,
    addReminder: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(modifier = Modifier.fillMaxWidth(),
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Title") })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(modifier = Modifier.fillMaxWidth(),
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") })

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            TextButton(onClick = { showDatePickerDialog(true) }) {
                Text(state.selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(onClick = { showTimePickerDialog(true) }) {
                Text(state.selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")))
            }
        }


        Box {
            TextButton(onClick = {
                showRepeatOption(!state.showRepeatOptions)
            }) {
                Text(text = "Repeat: ${repeatOptions[state.selectedRepeatOption]}")
            }
            DropdownMenu(expanded = state.showRepeatOptions, onDismissRequest = {
                showRepeatOption(false)
            }) {
                repeatOptions.forEach { option ->
                    DropdownMenuItem(onClick = {
                        repeatSelected(repeatOptions.indexOf(option))
                        showRepeatOption(false)
                    }) {
                        Text(text = option)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = addReminder,
            shape = RoundedCornerShape(50),
        ) {
            Text(
                text = "Save reminder",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        val context = LocalContext.current
        if (state.showDatePicker) {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                dateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                showDatePickerDialog(false)
            }
            val dialog = DatePickerDialog(
                context,
                dateSetListener,
                state.selectedDate.year,
                state.selectedDate.monthValue - 1,
                state.selectedDate.dayOfMonth
            )
            dialog.setOnDismissListener { showDatePickerDialog(false) }
            dialog.datePicker.minDate = System.currentTimeMillis()
            dialog.datePicker.maxDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)
            dialog.show()
        }

        if (state.showTimePicker) {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                timeSelected(LocalTime.of(hourOfDay, minute))
                showTimePickerDialog(false)
            }
            val dialog = TimePickerDialog(
                context,
                timeSetListener,
                state.selectedTime.hour,
                state.selectedTime.minute,
                false
            )
            dialog.setOnDismissListener { showTimePickerDialog(false) }
            dialog.show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddReminderPreview() {
    MyApplicationTheme {
        AddOrUpdateReminderScreenContent(
            modifier = Modifier,
            state = AddReminderViewModel.ScreenState(),
            onTitleChange = {},
            onDescriptionChange = {},
            showDatePickerDialog = {},
            showTimePickerDialog = {},
            timeSelected = {},
            dateSelected = {},
            showRepeatOption = {},
            repeatSelected = {}
        ) {}
    }
}