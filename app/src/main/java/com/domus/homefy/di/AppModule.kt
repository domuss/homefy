package com.domus.homefy.di

import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.house.HouseViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://ipmcgrdpahuotncgfryc.supabase.co",
            supabaseKey = "sb_publishable_ZlJ7DHomKkR-oTmkP26UVw_0qaGF2n-"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    single {
        AuthRepository(get())
    }
    single { HouseRepository(get()) }

    viewModel { AuthViewModel(get(), get()) }

    viewModel { HouseViewModel(get(), get()) }
}