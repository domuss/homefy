package com.domus.homefy.ui.bill

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.Bill
import com.domus.homefy.data.BillRepository
import com.domus.homefy.data.HouseMemberOption
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.UserRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

sealed interface BillUIStatus {
    object Esperando : BillUIStatus
    object Loading : BillUIStatus
    object Sucesso : BillUIStatus
    data class Error(val message: String) : BillUIStatus
}

class BillViewModel(
    private val billRepository: BillRepository,
    private val houseRepository: HouseRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var uiStatus by mutableStateOf<BillUIStatus>(BillUIStatus.Esperando)
        private set

    var members by mutableStateOf<List<HouseMemberOption>>(emptyList())
        private set

    var bills by mutableStateOf<List<Bill>>(emptyList())
        private set

    fun clearStatus() {
        uiStatus = BillUIStatus.Esperando
    }

    fun loadMembers(houseId: Long) {
        viewModelScope.launch {
            val result = houseRepository.getMembersByHouse(houseId)

            if (result.isSuccess) {
                members = result.getOrNull() ?: emptyList()
            } else {
                uiStatus = BillUIStatus.Error("Erro ao buscar membros da casa")
            }
        }
    }

    fun loadBills(houseId: Long) {
        viewModelScope.launch {
            uiStatus = BillUIStatus.Loading

            val result = billRepository.getBillsByHouse(houseId)

            if (result.isSuccess) {
                bills = result.getOrNull() ?: emptyList()
                uiStatus = BillUIStatus.Sucesso
            } else {
                uiStatus = BillUIStatus.Error("Erro ao carregar contas")
            }
        }
    }

    fun createBill(
        houseId: Long?,
        responsibleId: Long?,
        title: String,
        description: String,
        amount: String,
        dueDate: String
    ) {
        if (houseId == null) {
            uiStatus = BillUIStatus.Error("Selecione uma casa")
            return
        }

        if (title.isBlank()) {
            uiStatus = BillUIStatus.Error("O título da conta não pode ficar vazio")
            return
        }

        val amountCents = parseAmountCents(amount)
        if (amountCents == null) {
            uiStatus = BillUIStatus.Error("Informe um valor válido")
            return
        }

        val normalizedDueDate = normalizeDueDate(dueDate)
        if (dueDate.isNotBlank() && normalizedDueDate == null) {
            uiStatus = BillUIStatus.Error("Use a data no formato AAAA-MM-DD")
            return
        }

        viewModelScope.launch {
            uiStatus = BillUIStatus.Loading

            val createdBy = getCurrentPublicUserId()
            if (createdBy == null) {
                uiStatus = BillUIStatus.Error("Erro ao identificar usuário atual")
                return@launch
            }

            val bill = Bill(
                house_id = houseId,
                title = title.trim(),
                description = description.trim().ifBlank { null },
                amount_cents = amountCents,
                due_date = normalizedDueDate,
                responsible_id = responsibleId,
                created_by = createdBy
            )

            val result = billRepository.createBill(bill)

            uiStatus = if (result.isSuccess) {
                BillUIStatus.Sucesso
            } else {
                BillUIStatus.Error("Erro ao criar conta: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun toggleBillPaid(bill: Bill, checked: Boolean) {
        val billId = bill.id ?: return

        viewModelScope.launch {
            val result = billRepository.updateBillPaid(billId, checked)

            if (result.isSuccess) {
                bills = bills.map {
                    if (it.id == billId) it.copy(is_paid = checked) else it
                }
            } else {
                uiStatus = BillUIStatus.Error("Erro ao atualizar conta")
            }
        }
    }

    fun updateBill(
        bill: Bill,
        title: String,
        description: String,
        amount: String,
        dueDate: String,
        responsibleId: Long?,
        isPaid: Boolean
    ) {
        if (title.isBlank()) {
            uiStatus = BillUIStatus.Error("O título da conta não pode ficar vazio")
            return
        }

        val amountCents = parseAmountCents(amount)
        if (amountCents == null) {
            uiStatus = BillUIStatus.Error("Informe um valor válido")
            return
        }

        val normalizedDueDate = normalizeDueDate(dueDate)
        if (dueDate.isNotBlank() && normalizedDueDate == null) {
            uiStatus = BillUIStatus.Error("Use a data no formato AAAA-MM-DD")
            return
        }

        viewModelScope.launch {
            uiStatus = BillUIStatus.Loading

            val updatedBill = bill.copy(
                title = title.trim(),
                description = description.trim().ifBlank { null },
                amount_cents = amountCents,
                due_date = normalizedDueDate,
                responsible_id = responsibleId,
                is_paid = isPaid
            )

            val result = billRepository.updateBill(updatedBill)

            uiStatus = if (result.isSuccess) {
                bills = bills.map {
                    if (it.id == updatedBill.id) updatedBill else it
                }
                BillUIStatus.Sucesso
            } else {
                BillUIStatus.Error("Erro ao atualizar conta")
            }
        }
    }

    fun deleteBill(bill: Bill, onDeleted: () -> Unit) {
        val billId = bill.id ?: return

        viewModelScope.launch {
            uiStatus = BillUIStatus.Loading

            val result = billRepository.deleteBill(billId)

            if (result.isSuccess) {
                bills = bills.filterNot { it.id == billId }
                uiStatus = BillUIStatus.Sucesso
                onDeleted()
            } else {
                uiStatus = BillUIStatus.Error("Erro ao excluir conta")
            }
        }
    }

    private suspend fun getCurrentPublicUserId(): Long? {
        val authUserInfo = authRepository.getCurrentUser() ?: return null
        return userRepository.getUserBySupaId(authUserInfo.id).getOrNull()?.id
    }

    private fun normalizeDueDate(dueDate: String): String? {
        val trimmed = dueDate.trim()
        if (trimmed.isBlank()) {
            return null
        }

        return runCatching {
            LocalDate.parse(trimmed).toString()
        }.getOrNull()
    }

    private fun parseAmountCents(amount: String): Long? {
        val cleaned = amount.trim()
            .replace("R$", "")
            .replace(" ", "")

        if (cleaned.isBlank()) {
            return null
        }

        val normalized = if (cleaned.contains(",")) {
            cleaned.replace(".", "").replace(",", ".")
        } else {
            cleaned
        }

        val value = normalized.toBigDecimalOrNull() ?: return null
        if (value < BigDecimal.ZERO) {
            return null
        }

        return runCatching {
            value.multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull()
    }
}
