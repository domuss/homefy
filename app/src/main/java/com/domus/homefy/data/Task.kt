package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long? = null,
    val house_id: Long,
    val title: String,
    val description: String? = null,
    val assignee_id: Long? = null,
    val is_completed: Boolean = false
)
data class HouseMemberOption(
    val memberId: Long,
    val userId: Int,
    val name: String,
    val username: String?
)