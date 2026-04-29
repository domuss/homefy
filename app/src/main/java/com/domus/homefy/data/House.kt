package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: Long?=null,
    val name: String,
    val creator_id: Int,
    val access_code: String? = null,
    val is_code_active: Boolean? = null
)
