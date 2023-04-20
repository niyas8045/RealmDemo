package com.mongodb.app.data

import com.mongodb.app.domain.IReminderRepository
import com.mongodb.app.presentation.Reminder
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class ReminderRepository : IReminderRepository {

    companion object {
        @Volatile
        private var INSTANCE: ReminderRepository? = null

        fun getInstance(): ReminderRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ReminderRepository()
                INSTANCE = instance
                instance
            }
        }
    }

    private var realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(ReminderRealmObject::class))
        realm = Realm.open(config)
    }

    override suspend fun addReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        realm.write {
            val reminderRealmObject = reminderToRealmObject(reminder)
            copyToRealm(
                reminderRealmObject
            )
        }
    }

    override suspend fun getReminder(id: ObjectId) = withContext(Dispatchers.IO) {
        realm.query<ReminderRealmObject>("_id == $0", id).first()
            .find()
    }


    override fun getReminders(): Flow<ResultsChange<ReminderRealmObject>> {
        return realm.query<ReminderRealmObject>().sort(Pair("_id", Sort.DESCENDING))
            .asFlow()
    }

    override suspend fun updateReminder(id: ObjectId, reminder: Reminder) =
        withContext(Dispatchers.IO) {
            val realmObject = reminderToRealmObject(reminder)
            realm.write {
                val item = query<ReminderRealmObject>("_id == $0", id).first()
                    .find()
                item?.let {
                    it.title = realmObject.title
                    it.description = realmObject.description
                    it.dateTime = realmObject.dateTime
                    it.repeatInterval = realmObject.repeatInterval
                }
            }
        }

    override suspend fun deleteReminder(realmObject: ReminderRealmObject) =
        withContext(Dispatchers.IO) {
            realm.write {
                findLatest(realmObject)?.let {
                    delete(it)
                }
            }
        }

    private fun reminderToRealmObject(reminder: Reminder): ReminderRealmObject {
        val dateTime = LocalDateTime.of(reminder.date, reminder.time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .let { Date.from(it) }
        return ReminderRealmObject().apply {
            title = reminder.title
            description = reminder.description
            this.dateTime = dateTime.time
            repeatInterval = reminder.repeatInterval
        }
    }
}