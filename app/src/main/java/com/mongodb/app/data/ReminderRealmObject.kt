package com.mongodb.app.data


import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReminderRealmObject : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var title: String = ""
    var description: String = ""
    var dateTime: Long = 0L
    var repeatInterval: Int = 0

    @Ignore
    var dateTimeText: String = ""
}