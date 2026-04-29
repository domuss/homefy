package com.domus.homefy.di

import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.UserRepository
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.auth.signup.SignUpViewModel
import com.domus.homefy.ui.house.HouseViewModel
import com.domus.homefy.ui.profile.EditProfileViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

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

    single { UserRepository(get()) }
    single { AuthRepository(get(), get()) }  // SupabaseClient + UserRepository
    single { HouseRepository(get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { EditProfileViewModel(get(), get()) }  // AuthRepository + UserRepository
    viewModel { HouseViewModel(get(), get()) }        // sem mudança
}