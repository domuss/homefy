package com.domus.homefy.di

import androidx.room.Room
import com.domus.homefy.data.AuthRepository
import com.domus.homefy.data.BillRepository
import com.domus.homefy.data.DailyQuoteRepository
import com.domus.homefy.data.HomefyDatabase
import com.domus.homefy.data.HouseRepository
import com.domus.homefy.data.KanyeQuoteApi
import com.domus.homefy.data.TaskRepository
import com.domus.homefy.data.UserRepository
import com.domus.homefy.ui.auth.AuthViewModel
import com.domus.homefy.ui.auth.signup.SignUpViewModel
import com.domus.homefy.ui.bill.BillViewModel
import com.domus.homefy.ui.house.HouseViewModel
import com.domus.homefy.ui.profile.ProfileViewModel
import com.domus.homefy.ui.quote.DailyQuoteViewModel
import com.domus.homefy.ui.task.TaskViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    single { AuthRepository(get(), get()) }
    single { HouseRepository(get()) }
    single { TaskRepository(get()) }
    single { BillRepository(get()) }

    single {
        Retrofit.Builder()
            .baseUrl("https://api.kanye.rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single { get<Retrofit>().create(KanyeQuoteApi::class.java) }
    single {
        Room.databaseBuilder(
            androidContext(),
            HomefyDatabase::class.java,
            "homefy.db"
        ).build()
    }
    single { get<HomefyDatabase>().dailyQuoteDao() }
    single { DailyQuoteRepository(get(), get()) }

    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { HouseViewModel(get(), get(), get()) }
    viewModel { TaskViewModel(get(), get()) }
    viewModel { BillViewModel(get(), get(), get(), get()) }
    viewModel { DailyQuoteViewModel(get()) }
}
