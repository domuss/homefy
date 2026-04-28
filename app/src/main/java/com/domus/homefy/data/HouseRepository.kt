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

    suspend fun getHousesByUser(userId: String): Result<List<House>> {
        return try {
            val houses = supabase.postgrest["home"].select {
                filter {
                    eq("creator_id", userId) // Traz só as casas desse usuário
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

}