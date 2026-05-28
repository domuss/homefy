package com.domus.homefy.ui.quote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domus.homefy.data.DailyQuoteEntity
import com.domus.homefy.data.DailyQuoteRepository
import kotlinx.coroutines.launch

sealed interface DailyQuoteUIStatus {
    object Idle : DailyQuoteUIStatus
    object Loading : DailyQuoteUIStatus
    data class Success(val quote: DailyQuoteEntity) : DailyQuoteUIStatus
    data class Error(val message: String) : DailyQuoteUIStatus
}

class DailyQuoteViewModel(
    private val dailyQuoteRepository: DailyQuoteRepository
) : ViewModel() {

    var uiStatus by mutableStateOf<DailyQuoteUIStatus>(DailyQuoteUIStatus.Idle)
        private set

    fun loadDailyQuote() {
        if (uiStatus is DailyQuoteUIStatus.Loading || uiStatus is DailyQuoteUIStatus.Success) {
            return
        }

        viewModelScope.launch {
            uiStatus = DailyQuoteUIStatus.Loading
            val result = dailyQuoteRepository.getTodayQuote()

            uiStatus = if (result.isSuccess) {
                DailyQuoteUIStatus.Success(result.getOrThrow())
            } else {
                DailyQuoteUIStatus.Error(
                    result.exceptionOrNull()?.message ?: "Erro ao carregar frase do dia"
                )
            }
        }
    }
}
