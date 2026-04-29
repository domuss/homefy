package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val supa_id: String,
    val name: String,
    val username: String
)