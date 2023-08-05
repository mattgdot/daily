package com.app.daily.di

import android.content.Context
import android.content.SharedPreferences
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.domain.repositories.ListsRepository
import com.app.daily.domain.repositories.SharedPreferencesRepository
import com.app.daily.domain.repositories.UsersRepository
import com.app.daily.utils.Constants.LISTS_COLLECTION
import com.app.daily.utils.Constants.USERS_COLLECTION
import com.app.daily.utils.Constants.SHARED_PREFERENCES_NAME
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Named("users_ref")
    fun provideUsersRef() = Firebase.firestore.collection(USERS_COLLECTION)

    @Provides
    @Named("lists_ref")
    fun provideListsRef() = Firebase.firestore.collection(LISTS_COLLECTION)

    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = context.applicationContext.getSharedPreferences(
        SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
    )

    @Provides
    fun provideSharedPreferencesRepository(
        sharedPreferences: SharedPreferences
    ): SharedPreferencesRepository = SharedPreferencesRepositoryImpl(sharedPreferences)

    @Provides
    fun provideUsersRepository(
        usersRef: CollectionReference
    ): UsersRepository = UsersRepositoryImpl(usersRef)

    @Provides
    fun provideListsRepository(
        listsRef: CollectionReference
    ): ListsRepository = ListsRepositoryImpl(listsRef)
}