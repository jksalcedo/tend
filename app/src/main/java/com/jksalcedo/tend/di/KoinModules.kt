package com.jksalcedo.tend.di

import androidx.room.Room
import com.jksalcedo.tend.data.local.AppDatabase
import com.jksalcedo.tend.data.repository.PersonRepositoryImpl
import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import com.jksalcedo.tend.domain.usecase.CheckInUseCase
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import com.jksalcedo.tend.domain.usecase.GetUpcomingCheckInsUseCase
import com.jksalcedo.tend.ui.add.AddPersonViewModel
import com.jksalcedo.tend.ui.detail.PersonDetailViewModel
import com.jksalcedo.tend.ui.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "tend_database"
        ).fallbackToDestructiveMigration()
            .build()
    }
    
    single { get<AppDatabase>().checkInDao() }
    
    single<PersonRepository> { PersonRepositoryImpl(get()) }
    
    factory { GetUpcomingCheckInsUseCase(get()) }
    factory { GetPersonUseCase(get()) }
    factory { AddPersonUseCase(get()) }
    factory { CheckInUseCase(get()) }
    
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddPersonViewModel)
    viewModelOf(::PersonDetailViewModel)
}
