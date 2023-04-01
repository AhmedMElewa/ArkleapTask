package com.elewa.arkleaptask.core.model

import androidx.annotation.StringRes
import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemUiModel

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
sealed class ResourceUiState {
    object Empty : ResourceUiState()
    object Loading : ResourceUiState()
    class Loaded(val itemState: ItemUiModel) : ResourceUiState()
    class Error(@StringRes val message: Int) : ResourceUiState()
    class PrinterState(@StringRes val message: Int) : ResourceUiState()
}