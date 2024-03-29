package com.tarripoha.android.domain.entity

import com.google.firebase.database.DatabaseException

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val password: String? = null,
    var emailVerified: Boolean? = null,
    val city: String? = null,
    var dirty: Boolean? = null,
    val admin: Boolean = false,
    val timestamp: Long,
    var fcmToken: String? = null,
    var lastSessionTime: Long? = null,
) {

    /** Must define a no-argument constructor to avoid [DatabaseException] */
    constructor() : this(
        id = "", name = "", phone = "", email = "", timestamp = 0L
    )

    constructor(
        id: String,
        name: String,
        phone: String,
        email: String,
        timestamp: Long,
    ) : this(
        id = id, name = name, phone = phone, email = email, timestamp = timestamp, dirty = false
    )
}
