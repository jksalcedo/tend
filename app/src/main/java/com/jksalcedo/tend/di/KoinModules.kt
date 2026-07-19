package com.jksalcedo.tend.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jksalcedo.tend.data.contacts.NativeContactsDataSource
import com.jksalcedo.tend.data.local.AppDatabase
import com.jksalcedo.tend.data.local.MIGRATION_3_6
import com.jksalcedo.tend.data.local.MIGRATION_6_7
import com.jksalcedo.tend.data.local.MIGRATION_7_8
import com.jksalcedo.tend.data.repository.CheckInHistoryRepositoryImpl
import com.jksalcedo.tend.data.repository.ContactsRepositoryImpl
import com.jksalcedo.tend.data.repository.OnboardingRepositoryImpl
import com.jksalcedo.tend.data.repository.PersonRepositoryImpl
import com.jksalcedo.tend.data.repository.TagRepositoryImpl
import com.jksalcedo.tend.domain.repository.CheckInHistoryRepository
import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.OnboardingRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.domain.repository.TagRepository
import com.jksalcedo.tend.domain.usecase.AddNoteUseCase
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import com.jksalcedo.tend.domain.usecase.AddTagToPersonUseCase
import com.jksalcedo.tend.domain.usecase.ArchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.CheckInUseCase
import com.jksalcedo.tend.domain.usecase.DeleteNoteUseCase
import com.jksalcedo.tend.domain.usecase.DeletePersonUseCase
import com.jksalcedo.tend.domain.usecase.DeleteTagUseCase
import com.jksalcedo.tend.domain.usecase.GetArchivedPeopleUseCase
import com.jksalcedo.tend.domain.usecase.GetImportableContactsUseCase
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import com.jksalcedo.tend.domain.usecase.GetUpcomingCheckInsUseCase
import com.jksalcedo.tend.domain.usecase.GetWeeklyInsightsUseCase
import com.jksalcedo.tend.domain.usecase.ImportContactsUseCase
import com.jksalcedo.tend.domain.usecase.MaybeShowContactImportPromptUseCase
import com.jksalcedo.tend.domain.usecase.ObserveAllTagsUseCase
import com.jksalcedo.tend.domain.usecase.ObserveDuplicatePeopleUseCase
import com.jksalcedo.tend.domain.usecase.ObservePersonUseCase
import com.jksalcedo.tend.domain.usecase.RefreshLinkedContactsUseCase
import com.jksalcedo.tend.domain.usecase.RemoveTagFromPersonUseCase
import com.jksalcedo.tend.domain.usecase.ResolveContactImportPromptUseCase
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
        ).addMigrations(MIGRATION_3_6, MIGRATION_6_7, MIGRATION_7_8)
            // Last-resort fallback only for installs older than version 3 (predating
            // any migration path this app has ever shipped) — everything from 3 onward
            // goes through a real Migration so upgrading never silently wipes data.
            .fallbackToDestructiveMigration()
            // Seeds the two default tags for a genuinely fresh install (a DB created
            // directly at the current version never runs MIGRATION_3_6, which only
            // fires for installs upgrading from an earlier version).
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT OR IGNORE INTO tags (name) VALUES ('Family')")
                    db.execSQL("INSERT OR IGNORE INTO tags (name) VALUES ('Friend')")
                }
            })
            .build()
    }

    single { get<AppDatabase>().checkInDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().checkInHistoryDao() }

    single<PersonRepository> { PersonRepositoryImpl(get(), androidContext()) }
    single<TagRepository> { TagRepositoryImpl(get()) }
    single<CheckInHistoryRepository> { CheckInHistoryRepositoryImpl(get()) }

    single { NativeContactsDataSource(androidContext()) }
    single<ContactsRepository> { ContactsRepositoryImpl(get(), get()) }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("tend_prefs") }
        )
    }
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }

    factory { GetUpcomingCheckInsUseCase(get()) }
    factory { GetWeeklyInsightsUseCase(get()) }
    factory { MaybeShowContactImportPromptUseCase(get(), get()) }
    factory { ResolveContactImportPromptUseCase(get()) }
    factory { GetPersonUseCase(get()) }
    factory { ObservePersonUseCase(get()) }
    factory { ObserveDuplicatePeopleUseCase(get()) }
    factory { AddPersonUseCase(get()) }
    factory { CheckInUseCase(get(), get()) }
    factory { AddNoteUseCase(get()) }
    factory { UpdatePersonUseCase(get()) }
    factory { ArchivePersonUseCase(get()) }
    factory { DeletePersonUseCase(get()) }
    factory { GetArchivedPeopleUseCase(get()) }
    factory { UnarchivePersonUseCase(get()) }
    factory { GetImportableContactsUseCase(get()) }
    factory { ImportContactsUseCase(get(), get()) }
    factory { RefreshLinkedContactsUseCase(get(), get()) }
    factory { UnlinkPersonUseCase(get()) }
    // single, not factory: its per-person lock map must be the same instance for every caller
    // to actually guard against concurrent invocations for the same person.
    single { SyncToDeviceUseCase(get(), get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { UpdateNoteUseCase(get()) }
    factory { ObserveAllTagsUseCase(get()) }
    factory { AddTagToPersonUseCase(get(), get()) }
    factory { RemoveTagFromPersonUseCase(get()) }
    factory { DeleteTagUseCase(get(), get()) }

    factory { com.jksalcedo.tend.domain.usecase.ExportDataUseCase(get()) }
    factory { com.jksalcedo.tend.domain.usecase.ImportDataUseCase(get()) }
    factory { com.jksalcedo.tend.domain.usecase.GetNerdStatsUseCase(androidContext(), get()) }
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddPersonViewModel)
    viewModelOf(::PersonDetailViewModel)
    viewModelOf(::ArchivedViewModel)
    viewModelOf(::ImportContactsViewModel)
}
