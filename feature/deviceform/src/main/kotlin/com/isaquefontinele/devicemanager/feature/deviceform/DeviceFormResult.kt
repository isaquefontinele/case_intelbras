package com.isaquefontinele.devicemanager.feature.deviceform

/** How the form was closed, so the previous screen can confirm it. `null` means it was dismissed. */
enum class DeviceFormResult { ADDED, UPDATED, SAVED_OFFLINE, NOT_FOUND }
