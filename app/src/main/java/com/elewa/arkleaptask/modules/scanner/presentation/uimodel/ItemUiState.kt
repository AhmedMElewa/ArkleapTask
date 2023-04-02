package com.elewa.arkleaptask.core.model

import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemSideEffects
import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemUiModel

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
sealed class ItemUiState {
    object Empty : ItemUiState()

    data class Loading(val loading: Boolean) : ItemUiState()
    data class Loaded(val itemState: ItemUiModel) : ItemUiState()

}