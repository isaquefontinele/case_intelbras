package com.isaquefontinele.devicemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Stored because the sync can run with the app closed: the warning waits here until the list is opened. */
@Entity(tableName = "rejected_changes")
data class RejectedChangeEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val deviceName: String)
