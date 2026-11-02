package com.isaquefontinele.devicemanager.core.model

/** A pending change the server refused, waiting to be reported to the user. */
data class RejectedChange(val id: Long, val deviceName: String)
