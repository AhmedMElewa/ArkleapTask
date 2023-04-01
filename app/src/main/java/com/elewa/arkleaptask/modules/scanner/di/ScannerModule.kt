package com.elewa.arkleaptask.modules.scanner.di

import com.elewa.arkleaptask.modules.scanner.data.repository.ScannerRepositoryImpl
import com.elewa.arkleaptask.modules.scanner.domain.repository.ScannerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class ScannerModule {

    @Binds
    abstract fun bindScannerRepository(repository: ScannerRepositoryImpl): ScannerRepository

}