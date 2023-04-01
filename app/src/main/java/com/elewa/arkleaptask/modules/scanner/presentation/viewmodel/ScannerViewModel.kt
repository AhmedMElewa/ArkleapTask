package com.elewa.arkleaptask.modules.scanner.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elewa.arkleaptask.R
import com.elewa.arkleaptask.core.model.DomainExceptions
import com.elewa.arkleaptask.core.model.ResourceUiState
import com.elewa.arkleaptask.modules.scanner.domain.entity.BarcodeException
import com.elewa.arkleaptask.modules.scanner.domain.interactor.ScanBarcode
import com.elewa.arkleaptask.modules.scanner.presentation.ui.toBarcodeBitmap
import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemUiModel
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.ImagePrintable
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.utilities.PrintingCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(private val scanBarcode: ScanBarcode) : ViewModel() {

    private val _uiState = MutableStateFlow<ResourceUiState>(ResourceUiState.Empty)
    val uiState: StateFlow<ResourceUiState> = _uiState

    fun scanBarcode(barcode: String) {
        _uiState.value = ResourceUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            scanBarcode.execute(barcode).fold({
                //delay for view of progress
                delay(500)
                _uiState.value = ResourceUiState.Loaded(it.mapToUiModel())
            }, {
                it.handleError()
            })
        }
    }

    fun setupPrinter(currentPrinterIp: String) {
        Printooth.setPrinter("printerName", currentPrinterIp)
    }


    fun printItem(currentItem: ItemUiModel) {

        if (Printooth.hasPairedPrinter()) {
            var printables = ArrayList<Printable>()
            printables.add(
                ImagePrintable.Builder(currentItem.barcode.toBarcodeBitmap())
                    .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                    .build()
            )
            printables.add(buildPrintText(currentItem.barcode))
            printables.add(buildPrintText(currentItem.store))
            printables.add(buildPrintText(currentItem.government))
            printables.add(buildPrintText(currentItem.area))
            printables.add(buildPrintText(currentItem.phoneNumber))
            Printooth.printer().print(printables)

            Printooth.printer().printingCallback = object : PrintingCallback {
                override fun connectingWithPrinter() {
                    Log.i("Elewa", "connected")
                }


                override fun printingOrderSentSuccessfully() {
                    Log.i("Elewa", "Success")
                    _uiState.value = ResourceUiState.PrinterState(R.string.printer_success)

                }  //printer was received your printing order successfully.

                override fun connectionFailed(error: String) {
                    Log.i("Elewa", error)
                    _uiState.value = ResourceUiState.PrinterState(R.string.printer_fail)
                }

                override fun disconnected() {
                    Log.i("Elewa", "disconnected")
                }

                override fun onError(error: String) {
                    Log.i("Elewa", error)
                    _uiState.value = ResourceUiState.PrinterState(R.string.pinter_error)

                }

                override fun onMessage(message: String) {
                    Log.i("Elewa", message)
                }
            }
        } else {

        }

    }

    private fun buildPrintText(text:String): Printable {
        return TextPrintable.Builder()
            .setText(text)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
            .setNewLinesAfter(1)
            .build()
    }

    private fun Throwable.handleError() {
        when (this@handleError) {
            is BarcodeException.BarcodeRequired -> _uiState.value =
                ResourceUiState.Error(R.string.barcode_required)
            is BarcodeException.BarcodeNotValid -> _uiState.value =
                ResourceUiState.Error(R.string.barcode_wrong)
            is DomainExceptions.UnknownException -> _uiState.value =
                ResourceUiState.Error(R.string.generic_error)
            else -> ResourceUiState.Error(R.string.generic_error)
        }
    }


}