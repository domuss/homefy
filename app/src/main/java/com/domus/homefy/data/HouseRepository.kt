package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class HouseRepository(private val supabase: SupabaseClient) {

    suspend fun  CriarCasa(house: House): Result<Unit>{
            return try {
                supabase.postgrest["home"].insert(house)
                Result.success(Unit)
            } catch (e: Exception){
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


    suspend fun insertMember(houseId: Long, userId: Int, roleId: Int = Role.RESIDENT.id): Result<Unit> {
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

    suspend fun getMembersByHouse(houseId: Long): Result<List<HouseMemberOption>> {
        return try {
            val members = supabase.postgrest["house_members"].select {
                filter {
                    eq("house_id", houseId)
                }
            }.decodeList<HouseMember>()

            val userIds = members.map { it.user_id }

            if (userIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val users = supabase.postgrest["users"].select {
                filter {
                    isIn("id", userIds)
                }
            }.decodeList<User>()

            val options = members.mapNotNull { member ->
                val memberId = member.id ?: return@mapNotNull null
                val user = users.firstOrNull { it.id?.toInt() == member.user_id }

                HouseMemberOption(
                    memberId = memberId,
                    userId = member.user_id,
                    name = user?.name ?: "Membro sem nome",
                    username = user?.username
                )
            }

            Result.success(options)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}