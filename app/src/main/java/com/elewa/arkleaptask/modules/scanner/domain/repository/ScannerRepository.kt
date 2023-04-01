package com.elewa.arkleaptask.modules.scanner.domain.repository

import com.elewa.arkleaptask.modules.scanner.data.model.ItemModel

interface ScannerRepository {

    suspend fun searchBarcode(barcode: String): Result<ItemModel>
}