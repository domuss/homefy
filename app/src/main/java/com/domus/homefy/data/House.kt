package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: Long?=null,
    val name: String,
    val creator_id: String
)
