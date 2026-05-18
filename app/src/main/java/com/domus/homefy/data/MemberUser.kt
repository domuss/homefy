package com.domus.homefy.data

import kotlinx.serialization.Serializable

@Serializable
data class MemberUser(
    val id: Int,
    val name: String
)
