package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class HouseMember(
    val id: Long? = null,
    val house_id: Long,
    val user_id: Int
)