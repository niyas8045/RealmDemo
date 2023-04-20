package com.mongodb.app.domain

import com.mongodb.app.data.ReminderRealmObject
import com.mongodb.app.presentation.Reminder
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

interface IReminderRepository {
    suspend fun addReminder(reminder: Reminder): ReminderRealmObject
    suspend fun getReminder(id: ObjectId): ReminderRealmObject?
    fun getReminders(): Flow<ResultsChange<ReminderRealmObject>>
    suspend fun updateReminder(id: ObjectId, reminder: Reminder): Unit?
    suspend fun deleteReminder(realmObject: ReminderRealmObject): Unit?
}