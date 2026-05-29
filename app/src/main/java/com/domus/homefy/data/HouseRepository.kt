package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class HouseRepository(private val supabase: SupabaseClient) {

    suspend fun CriarCasa(house: House): Result<Unit> {
        return try {
            supabase.postgrest["home"].insert(house)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHouseName(
        houseId: Long,
        newName: String
    ): Result<Unit> {
        return try {
            supabase.postgrest["home"].update(
                {
                    set("name", newName)
                }
            ) {
                filter {
                    eq("id", houseId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCodeStatus(houseId: Long, isActive: Boolean): Result<Unit> {
        return try {
            supabase.postgrest["home"].update(
                {
                    set("is_code_active", isActive)
                }
            ) {
                filter {
                    eq("id", houseId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHousesByUser(userId: Int): Result<List<House>> {
        return try {
            val houses = supabase.postgrest["home"].select {
                filter {
                    eq("creator_id", userId)
                }
            }.decodeList<House>()

            Result.success(houses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHouse(houseId: Long): Result<Unit> {
        return try {
            supabase.postgrest["home"]
                .delete {
                    filter {
                        eq("id", houseId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getHouseByAccessCode(code: String): Result<House?> {
        return try {
            val house = supabase.postgrest["home"].select {
                filter {
                    eq("access_code", code)
                }
            }.decodeSingleOrNull<House>()
            Result.success(house)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun insertMember(
        houseId: Long,
        userId: Int,
        roleId: Int = Role.RESIDENT.id
    ): Result<Unit> {
        return try {
            val member = HouseMember(house_id = houseId, user_id = userId, role_id = roleId)
            supabase.postgrest["house_members"].insert(member)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getJoinedHouses(userId: Int): Result<List<House>> {
        return try {
            val members = supabase.postgrest["house_members"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<HouseMember>()

            val houseIds = members.map { it.house_id }


            if (houseIds.isEmpty()) {
                return Result.success(emptyList())
            }


            val houses = supabase.postgrest["home"]
                .select {
                    filter {
                        isIn("id", houseIds)
                    }
                }.decodeList<House>()

            Result.success(houses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHouseMembers(houseId: Long): Result<List<HouseMemberFull>> {
        val columns = Columns.raw(
            """
            id,
            supa_id,
            name,
            username,
            house_members!inner(
                id,
                role_id
            )
        """.trimIndent()
        )

        return try {
            val membersRaw = supabase.from("users").select(
                columns = columns
            ) {
                filter {
                    eq("house_members.house_id", houseId)
                }
            }.decodeList<HouseMemberSupabase>()

            val members = mutableListOf<HouseMemberFull>()
            membersRaw.forEach {
                members.add(it.toModel())
            }

            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isHouseAdmin(houseId: Long, userId: Long): Boolean {
        val columns = Columns.raw(
            """
            id,
        """.trimIndent()
        )

        return try {
            supabase.from("house_members").select(columns = columns) {
                filter {
                    eq("house_id", houseId)
                    eq("user_id", userId)
                    eq("role_id", 1)
                }
            }.decodeSingleOrNull<Long>() != null
        } catch (e: Exception) {
            false
        }
    }
}