package com.lilbro.picobotella.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides Firebase singletons to the dependency graph.
 *
 * Installing in [SingletonComponent] ensures a single [FirebaseFirestore]
 * instance is shared across the entire application lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provides the [FirebaseFirestore] instance.
     *
     * Firebase SDK already manages its own internal singleton, but wrapping
     * it here lets Hilt inject it transparently and makes it easy to swap
     * with a fake during tests.
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
