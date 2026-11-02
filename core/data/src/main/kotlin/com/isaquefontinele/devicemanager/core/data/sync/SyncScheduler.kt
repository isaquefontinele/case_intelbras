package com.isaquefontinele.devicemanager.core.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal fun interface SyncScheduler {
    fun schedulePendingSync()
}

internal class WorkManagerSyncScheduler @Inject constructor(@ApplicationContext private val context: Context) :
    SyncScheduler {

    /**
     * Appends instead of keeping the existing work: a sync that is finishing has already read the pending
     * changes and would miss the new one. Scheduled work survives the app being closed and device restarts.
     */
    override fun schedulePendingSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    companion object {
        const val WORK_NAME = "sync_pending_devices"
        private const val BACKOFF_SECONDS = 30L
    }
}
