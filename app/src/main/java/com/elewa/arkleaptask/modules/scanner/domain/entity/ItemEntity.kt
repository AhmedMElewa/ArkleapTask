package com.elewa.arkleaptask.modules.scanner.domain.entity

import com.elewa.arkleaptask.modules.scanner.view.uimodel.ItemUiModel

data class ItemEntity(
    var barcode: String,
    var store: String,
    var government: String,
    var area: String,
    var phoneNumber: String
) {
    fun mapToUiModel(): ItemUiModel =
        ItemUiModel(
            barcode = barcode,
            store = store,
            government = government,
            area = area,
            phoneNumber = phoneNumber
        )
}
