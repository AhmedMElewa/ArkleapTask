package com.elewa.arkleaptask.modules.scanner.data.repository

import com.elewa.arkleaptask.modules.scanner.data.model.ItemModel
import com.elewa.arkleaptask.modules.scanner.domain.repository.ScannerRepository
import javax.inject.Inject

class ScannerRepositoryImpl @Inject constructor() : ScannerRepository {

    override suspend fun searchBarcode(barcode: String): Result<ItemModel> {
        return Result.success(ItemModel(barcode))
    }
}