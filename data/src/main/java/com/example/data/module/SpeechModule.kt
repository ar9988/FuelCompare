package com.example.data.module

import com.example.data.service.AndroidSpeechService
import com.example.domain.service.SpeechService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {
    @Binds
    @Singleton
    abstract fun bindSpeechService(
        impl: AndroidSpeechService
    ): SpeechService
}