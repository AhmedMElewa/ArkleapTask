package com.elewa.arkleaptask.modules.scanner.domain.interactor

import com.elewa.arkleaptask.base.BaseUseCase
import com.elewa.arkleaptask.core.model.DomainExceptions
import com.elewa.arkleaptask.modules.scanner.domain.entity.BarcodeException
import com.elewa.arkleaptask.modules.scanner.domain.entity.ItemEntity
import com.elewa.arkleaptask.modules.scanner.domain.repository.ScannerRepository
import javax.inject.Inject

class ScanBarcode @Inject constructor(private val scannerRepository: ScannerRepository) :
    BaseUseCase<String, Result<ItemEntity>> {

    override suspend fun execute(params: String?): Result<ItemEntity> {
        requireNotNull(params)
        return if (params.isEmpty()) {
            Result.failure(BarcodeException.BarcodeRequired)
        } else if (params?.length != 8) {
            Result.failure(BarcodeException.BarcodeNotValid)
        } else {
            scannerRepository.searchBarcode(params).fold({
                Result.success(it.mapToEntity())
            }, {
                Result.failure(DomainExceptions.UnknownException)
            })

        }
    }

}