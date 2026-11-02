package com.isaquefontinele.devicemanager.core.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.isaquefontinele.devicemanager.core.data.DataError
import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: DeviceRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val error = repository.syncPending().exceptionOrNull()
        return when (error) {
            null -> Result.success()
            // Retrying does not fix a bad token. The changes stay pending for the next refresh.
            is DataError.Unauthorized -> Result.failure()
            else -> Result.retry()
        }
    }
}
