package com.domus.homefy.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class BillRepository(private val supabase: SupabaseClient) {

    suspend fun createBill(bill: Bill): Result<Unit> {
        return try {
            supabase.postgrest["bills"].insert(bill)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBillsByHouse(houseId: Long): Result<List<Bill>> {
        return try {
            val bills = supabase.postgrest["bills"].select {
                filter {
                    eq("house_id", houseId)
                }
            }.decodeList<Bill>()

            Result.success(bills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBillPaid(billId: Long, isPaid: Boolean): Result<Unit> {
        return try {
            supabase.postgrest["bills"].update(
                {
                    set("is_paid", isPaid)
                }
            ) {
                filter {
                    eq("id", billId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBill(bill: Bill): Result<Unit> {
        val billId = bill.id ?: return Result.failure(Exception("Conta sem id"))

        return try {
            supabase.postgrest["bills"].update(
                {
                    set("title", bill.title)
                    set("description", bill.description)
                    set("amount_cents", bill.amount_cents)
                    set("due_date", bill.due_date)
                    set("is_paid", bill.is_paid)
                    set("responsible_id", bill.responsible_id)
                }
            ) {
                filter {
                    eq("id", billId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBill(billId: Long): Result<Unit> {
        return try {
            supabase.postgrest["bills"].delete {
                filter {
                    eq("id", billId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
