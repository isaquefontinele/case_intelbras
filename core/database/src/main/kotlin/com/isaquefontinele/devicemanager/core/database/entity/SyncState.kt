package com.isaquefontinele.devicemanager.core.database.entity

/**
 * Whether a local device row still has a change the server has not received. Pending rows are kept as they
 * are by a sync, so an offline change is not lost before it is sent.
 */
enum class SyncState {
    SYNCED,

    /** Created offline: the id is local and the server does not know the device yet. */
    PENDING_CREATE,
    PENDING_UPDATE,

    /** Removed offline: hidden from the app until the server confirms the removal. */
    PENDING_DELETE,
}
