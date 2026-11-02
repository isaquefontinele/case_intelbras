package com.isaquefontinele.devicemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), DeviceDatabase::class.java)

    @Test
    fun `migration 1 to 2 keeps devices and favorites and marks them as synced`() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                "INSERT INTO devices (id, type, name, password, macAddress) " +
                    "VALUES ('a1', 'ALARM', 'House', '1234', 'A1')",
            )
            db.execSQL("INSERT INTO favorites (deviceId) VALUES ('a1')")
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true).use { db ->
            db.query("SELECT name, syncState FROM devices").use { cursor ->
                cursor.moveToFirst()
                assertEquals("House", cursor.getString(0))
                assertEquals("SYNCED", cursor.getString(1))
            }
            db.query("SELECT COUNT(*) FROM favorites").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
            }
        }
    }

    @Test
    fun `migration 2 to 3 keeps devices and adds the id aliases`() {
        helper.createDatabase(DB_NAME, 2).use { db ->
            db.execSQL(
                "INSERT INTO devices (id, type, name, password, macAddress, syncState) " +
                    "VALUES ('local-1', 'ALARM', 'House', '1234', 'A1', 'PENDING_CREATE')",
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 3, true).use { db ->
            db.query("SELECT syncState FROM devices WHERE id = 'local-1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("PENDING_CREATE", cursor.getString(0))
            }
            db.query("SELECT COUNT(*) FROM device_id_aliases").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    @Test
    fun `migration 3 to 4 keeps pending devices and adds the rejected changes`() {
        helper.createDatabase(DB_NAME, 3).use { db ->
            db.execSQL(
                "INSERT INTO devices (id, type, name, password, macAddress, syncState) " +
                    "VALUES ('local-1', 'ALARM', 'House', '1234', 'A1', 'PENDING_CREATE')",
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 4, true).use { db ->
            db.query("SELECT syncState FROM devices WHERE id = 'local-1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("PENDING_CREATE", cursor.getString(0))
            }
            db.query("SELECT COUNT(*) FROM rejected_changes").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
