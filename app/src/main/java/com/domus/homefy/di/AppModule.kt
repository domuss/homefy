package com.domus.homefy.di

import com.domus.homefy.ui.home.LoginViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val appModule = module {
    viewModel { LoginViewModel() }
}