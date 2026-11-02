package com.isaquefontinele.devicemanager.core.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.isaquefontinele.devicemanager.core.data.DataError
import com.isaquefontinele.devicemanager.core.data.DeviceRepository
import com.isaquefontinele.devicemanager.core.data.WriteOutcome
import com.isaquefontinele.devicemanager.core.model.Device
import com.isaquefontinele.devicemanager.core.model.DeviceInput
import com.isaquefontinele.devicemanager.core.model.RejectedChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private suspend fun runWorker(syncResult: kotlin.Result<Unit>): Result {
        val repository = StubRepository(syncResult)
        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker = SyncWorker(appContext, workerParameters, repository)
        }
        return TestListenableWorkerBuilder<SyncWorker>(context).setWorkerFactory(factory).build().doWork()
    }

    @Test
    fun `a completed sync succeeds`() = runTest {
        assertEquals(Result.success(), runWorker(kotlin.Result.success(Unit)))
    }

    @Test
    fun `a sync that may work later is retried`() = runTest {
        assertEquals(Result.retry(), runWorker(kotlin.Result.failure(DataError.NoConnection())))
        assertEquals(Result.retry(), runWorker(kotlin.Result.failure(DataError.Unexpected())))
    }

    @Test
    fun `a rejected token is not retried`() = runTest {
        assertEquals(Result.failure(), runWorker(kotlin.Result.failure(DataError.Unauthorized())))
    }

    @Test
    fun `the scheduler waits for a network connection`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        WorkManagerSyncScheduler(context).schedulePendingSync()

        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WorkManagerSyncScheduler.WORK_NAME)
            .get()
            .single()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(NetworkType.CONNECTED, work.constraints.requiredNetworkType)
    }

    private class StubRepository(private val syncResult: kotlin.Result<Unit>) : DeviceRepository {
        override fun observeDevices(): Flow<List<Device>> = emptyFlow()
        override fun observeDevice(id: String): Flow<Device?> = emptyFlow()
        override fun observeRejectedChanges(): Flow<List<RejectedChange>> = emptyFlow()
        override suspend fun acknowledgeRejectedChange(id: Long) = Unit
        override suspend fun refresh(): kotlin.Result<Unit> = syncResult
        override suspend fun syncPending(): kotlin.Result<Unit> = syncResult
        override suspend fun create(input: DeviceInput) = kotlin.Result.success(WriteOutcome.SENT)
        override suspend fun update(id: String, input: DeviceInput) = kotlin.Result.success(WriteOutcome.SENT)
        override suspend fun delete(device: Device) = kotlin.Result.success(WriteOutcome.SENT)
        override suspend fun setFavorite(id: String, isFavorite: Boolean) = Unit
    }
}
