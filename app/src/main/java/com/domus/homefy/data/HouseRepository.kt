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


    suspend fun insertMember(houseId: Long, userId: Int): Result<Unit> {
        return try {
            val member = HouseMember(house_id = houseId, user_id = userId)
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


    suspend fun getHouseMembers(houseId: Long): Result<List<MemberUser>> {
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
            }.decodeList<MemberUser>()

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun removeMember(houseId: Long, userId: Int): Result<Unit> {
        return try {
            supabase.postgrest["house_members"].delete {
                filter {
                    eq("house_id", houseId)
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getTasksByHouse(houseId: Long): Result<List<Task>> {
        return try {
            val tasks = supabase.postgrest["tasks"].select {
                filter {
                    eq("house_id", houseId)
                }
            }.decodeList<Task>()

            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTask(task: Task): Result<Unit> {
        return try {
            supabase.postgrest["tasks"].insert(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean): Result<Unit> {
        return try {
            supabase.postgrest["tasks"].update(
                {
                    set("is_completed", isCompleted)
                }
            ) {
                filter {
                    eq("id", taskId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: Long): Result<Unit> {
        return try {
            supabase.postgrest["tasks"].delete {
                filter {
                    eq("id", taskId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOldestMember(houseId: Long, excludeUserId: Int): Result<HouseMember?> {
        return try {
            val members = supabase.postgrest["house_members"].select {
                filter { eq("house_id", houseId) }
            }.decodeList<HouseMember>()

            val oldest = members
                .filter { it.user_id != excludeUserId }
                .minByOrNull { it.joined_at ?: "" }

            Result.success(oldest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transfere a propriedade da casa para um novo creator_id.
     */
    suspend fun transferOwnership(houseId: Long, newCreatorId: Int): Result<Unit> {
        return try {
            supabase.postgrest["home"].update({ set("creator_id", newCreatorId) }) {
                filter { eq("id", houseId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove o usuário de todas as casas em que é membro.
     */
    suspend fun removeMemberFromAllHouses(userId: Int): Result<Unit> {
        return try {
            supabase.postgrest["house_members"].delete {
                filter { eq("user_id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}