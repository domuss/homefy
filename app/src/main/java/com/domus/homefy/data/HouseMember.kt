package com.domus.homefy.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class HouseMember(
    val id: Long? = null,
    val house_id: Long,
    val user_id: Int,
    val role_id: Int
)

@Serializable
data class HouseMemberFull(
    val id: Long,
    val user: User,
    val role: Role
)

object RoleSerializer : KSerializer<Role> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "Role",
        PrimitiveKind.INT
    )

    override fun serialize(
        encoder: Encoder,
        value: Role
    ) {
        encoder.encodeInt(value.id)
    }


    override fun deserialize(decoder: Decoder): Role {
        val id = decoder.decodeInt()
        return Role.entries.firstOrNull() {
            it.id == id
        } ?: error("Invalid role")
    }
}

@Serializable(with = RoleSerializer::class)
enum class Role(val id: Int) {
    HOUSE_ADMIN(1),
    RESIDENT(2)
}

@Serializable
data class HouseMemberSupabase(
    val id: Long,
    val supa_id: String,
    val name: String,
    val username: String,
    val house_members: List<HouseMembershipSupabase>
) {
    fun toModel(): HouseMemberFull {
        val membership = this.house_members.firstOrNull() ?: throw Exception("Usuário não pertence à nenhuma casa")

        return HouseMemberFull(
            membership.id,
            User(
                this.id,
                this.supa_id,
                this.name,
                this.username
            ),
            membership.role_id
        )
    }
}

@Serializable
data class HouseMembershipSupabase(
    val id: Long,
    val role_id: Role
)