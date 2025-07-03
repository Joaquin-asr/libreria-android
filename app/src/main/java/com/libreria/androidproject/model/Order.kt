package com.libreria.androidproject.model

import com.google.firebase.Timestamp

data class Order(
    var id: String = "",
    var userId: String = "",
    var libroId: String = "",
    var timestamp: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId"    to userId,
        "libroId"   to libroId,
        "timestamp" to timestamp
    )
}