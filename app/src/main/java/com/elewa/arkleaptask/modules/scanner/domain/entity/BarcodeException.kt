package com.elewa.arkleaptask.modules.scanner.domain.entity

sealed class BarcodeException:Throwable() {

    object BarcodeNotValid:BarcodeException()
    object BarcodeRequired:BarcodeException()

}