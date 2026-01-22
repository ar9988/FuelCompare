package com.example.data.module

import com.example.data.util.AndroidResourceProvider
import com.example.domain.util.ResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceModule {
    @Binds
    @Singleton
    abstract fun bindResourceProvider(
        resourceProvider: AndroidResourceProvider
    ): ResourceProvider
}