package com.elewa.arkleaptask.modules.scanner.presentation.uimodel

import androidx.annotation.StringRes
import com.elewa.arkleaptask.core.model.ItemUiState

sealed class ItemSideEffects {

    data class Error(@StringRes val message: Int) : ItemSideEffects()

    data class PrinterState(@StringRes val message: Int) : ItemSideEffects()
}
