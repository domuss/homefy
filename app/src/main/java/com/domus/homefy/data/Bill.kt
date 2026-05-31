package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    val id: Long? = null,
    val house_id: Long,
    val title: String,
    val description: String? = null,
    val amount_cents: Long,
    val due_date: String? = null,
    val is_paid: Boolean = false,
    val responsible_id: Long? = null,
    val created_by: Long
)
