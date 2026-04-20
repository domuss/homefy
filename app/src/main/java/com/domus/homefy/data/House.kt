package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: Long?=null,
    val nome: String,
    val creator_id: String
)
