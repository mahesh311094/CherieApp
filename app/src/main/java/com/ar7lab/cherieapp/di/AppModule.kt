package com.ar7lab.cherieapp.di

import android.content.Context
import com.ar7lab.cherieapp.base.navigation.NavManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     *  Provide SharePreferencesManager to save app's data
     */
    @Singleton
    @Provides
    fun provideSharePreferences(@ApplicationContext context: Context): SharePreferencesManager =
        SharePreferencesManager(context)

    @Singleton
    @Provides
    fun provideNavManager(): NavManager = NavManager()

}