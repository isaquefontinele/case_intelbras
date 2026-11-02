package com.isaquefontinele.devicemanager.core.insights

import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceSummaryTest {

    @Test
    fun noDevicesGiveAnEmptySummary() {
        assertEquals(DeviceSummary(), summarize(emptyList()))
    }

    @Test
    fun countsEachTypeAndTheFavorites() {
        val devices = listOf(
            DeviceInfo(isCamera = true, isFavorite = true),
            DeviceInfo(isCamera = true, isFavorite = false),
            DeviceInfo(isCamera = false, isFavorite = true),
        )

        assertEquals(DeviceSummary(cameras = 2, alarms = 1, favorites = 2), summarize(devices))
    }
}
