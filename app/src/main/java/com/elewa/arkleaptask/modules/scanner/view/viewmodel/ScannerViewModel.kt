package com.elewa.arkleaptask.modules.scanner.view.viewmodel

import android.print.PrintAttributes
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elewa.arkleaptask.R
import com.elewa.arkleaptask.core.model.DomainExceptions
import com.elewa.arkleaptask.core.model.ResourceUiState
import com.elewa.arkleaptask.modules.scanner.domain.entity.BarcodeException
import com.elewa.arkleaptask.modules.scanner.domain.interactor.ScanBarcode
import com.elewa.arkleaptask.modules.scanner.view.uimodel.ItemUiModel
import com.elewa.arkleaptask.util.PDFDocumentAdapter
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.PrintingImagesHelper
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.utilities.PrintingCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(private val scanBarcode: ScanBarcode) : ViewModel() {

    private val _uiState = MutableStateFlow<ResourceUiState>(ResourceUiState.Empty)
    val uiState: StateFlow<ResourceUiState> = _uiState

    fun scanBarcode(barcode: String) {
        _uiState.value = ResourceUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            scanBarcode.execute(barcode).fold({
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

        if (Printooth.hasPairedPrinter()){
            var printables = ArrayList<Printable>()
            var printable = TextPrintable.Builder()

                .setText("Hello World")
                .build()
            printables.add(printable)
            Printooth.printer().print(printables)

            Printooth.printer().printingCallback = object : PrintingCallback {
                override fun connectingWithPrinter() {
                    Log.i("Elewa","connected")
                }


                override fun printingOrderSentSuccessfully() {
                    Log.i("Elewa","Success")
                }  //printer was received your printing order successfully.

                override fun connectionFailed(error: String) {
                    Log.i("Elewa",error)
                }

                override fun disconnected() {
                    Log.i("Elewa","disconnected")
                }

                override fun onError(error: String) {
                    Log.i("Elewa",error)
                }

                override fun onMessage(message: String) {
                    Log.i("Elewa",message)
                }
            }
        }else{

        }

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