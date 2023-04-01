package com.elewa.arkleaptask.modules.scanner.data.model

import com.elewa.arkleaptask.modules.scanner.domain.entity.ItemEntity

data class ItemModel(
    var barcode: String,
    var store: String = "ياندا ستور",
    var government: String = "القاهرة",
    var area: String = "شبرا الخميه",
    var phoneNumber: String = "01128717171"
) {
    fun mapToEntity(): ItemEntity =
        ItemEntity(
            barcode = barcode,
            store = store,
            government = government,
            area = area,
            phoneNumber = phoneNumber
        )
}
