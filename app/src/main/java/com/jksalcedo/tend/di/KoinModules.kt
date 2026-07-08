package com.jksalcedo.tend.di

import androidx.room.Room
import com.jksalcedo.tend.data.contacts.NativeContactsDataSource
import com.jksalcedo.tend.data.local.AppDatabase
import com.jksalcedo.tend.data.repository.ContactsRepositoryImpl
import com.jksalcedo.tend.data.repository.PersonRepositoryImpl
import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.domain.usecase.AddNoteUseCase
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import com.jksalcedo.tend.domain.usecase.ArchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.CheckInUseCase
import com.jksalcedo.tend.domain.usecase.DeleteNoteUseCase
import com.jksalcedo.tend.domain.usecase.DeletePersonUseCase
import com.jksalcedo.tend.domain.usecase.GetArchivedPeopleUseCase
import com.jksalcedo.tend.domain.usecase.GetImportableContactsUseCase
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import com.jksalcedo.tend.domain.usecase.GetUpcomingCheckInsUseCase
import com.jksalcedo.tend.domain.usecase.ImportContactsUseCase
import com.jksalcedo.tend.domain.usecase.ObservePersonUseCase
import com.jksalcedo.tend.domain.usecase.RefreshLinkedContactsUseCase
import com.jksalcedo.tend.domain.usecase.SyncToDeviceUseCase
import com.jksalcedo.tend.domain.usecase.UnarchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.UnlinkPersonUseCase
import com.jksalcedo.tend.domain.usecase.UpdateNoteUseCase
import com.jksalcedo.tend.domain.usecase.UpdatePersonUseCase
import com.jksalcedo.tend.ui.add.AddPersonViewModel
import com.jksalcedo.tend.ui.archived.ArchivedViewModel
import com.jksalcedo.tend.ui.detail.PersonDetailViewModel
import com.jksalcedo.tend.ui.home.HomeViewModel
import com.jksalcedo.tend.ui.importcontacts.ImportContactsViewModel
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

    single { NativeContactsDataSource(androidContext()) }
    single<ContactsRepository> { ContactsRepositoryImpl(get(), get()) }

    factory { GetUpcomingCheckInsUseCase(get()) }
    factory { GetPersonUseCase(get()) }
    factory { ObservePersonUseCase(get()) }
    factory { AddPersonUseCase(get()) }
    factory { CheckInUseCase(get()) }
    factory { AddNoteUseCase(get()) }
    factory { UpdatePersonUseCase(get()) }
    factory { ArchivePersonUseCase(get()) }
    factory { DeletePersonUseCase(get()) }
    factory { GetArchivedPeopleUseCase(get()) }
    factory { UnarchivePersonUseCase(get()) }
    factory { GetImportableContactsUseCase(get()) }
    factory { ImportContactsUseCase(get()) }
    factory { RefreshLinkedContactsUseCase(get(), get()) }
    factory { UnlinkPersonUseCase(get()) }
    factory { SyncToDeviceUseCase(get(), get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { UpdateNoteUseCase(get()) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::AddPersonViewModel)
    viewModelOf(::PersonDetailViewModel)
    viewModelOf(::ArchivedViewModel)
    viewModelOf(::ImportContactsViewModel)
}
