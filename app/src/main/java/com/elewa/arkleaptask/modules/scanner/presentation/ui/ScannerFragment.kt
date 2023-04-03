package com.elewa.arkleaptask.modules.scanner.presentation.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.elewa.arkleaptask.R
import com.elewa.arkleaptask.base.BaseFragment
import com.elewa.arkleaptask.core.model.ItemUiState
import com.elewa.arkleaptask.core.preference.PrefsName.PRINTER_IP
import com.elewa.arkleaptask.core.preference.SharedPrefsManager
import com.elewa.arkleaptask.databinding.FragmentScannerBinding
import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemSideEffects
import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemUiModel
import com.elewa.arkleaptask.modules.scanner.presentation.viewmodel.ScannerViewModel
import com.elewa.arkleaptask.util.PDFConverter
import com.elewa.arkleaptask.util.PDFDocumentAdapter
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.ui.ScanningActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class ScannerFragment : BaseFragment<FragmentScannerBinding>() {

    private val viewModel: ScannerViewModel by viewModels()

    @Inject
    lateinit var prefs: SharedPrefsManager

    private var currentItem: ItemUiModel? = null

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentScannerBinding
        get() = FragmentScannerBinding::inflate

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (hasConnectBluetooth() && hasScanBluetooth() && hasAccessAresLocation()) {
                printBluetooth()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObservers()
        initEffectObservation()
    }

    private fun initView() {

        binding.etxtBarcode.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.scanBarcode(binding.etxtBarcode.text.toString())
                true
            } else {
                false
            }
        }

        binding.btnPrintPDF.setOnClickListener {
            if (currentItem != null) {
                val pdfConverter = PDFConverter()
                print(
                    pdfConverter.createPdf(
                        requireContext(),
                        currentItem!!,
                        requireActivity()
                    )
                )
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.scan_barcode),
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

        binding.btnPrintBluetooth.setOnClickListener {
            if (currentItem != null) {
                if (isBluetoothEnabled()) {
                    if (hasConnectBluetooth() && hasScanBluetooth() && hasAccessAresLocation()) {
                        printBluetooth()
                    } else {
                        requestPermissions()
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.permission_for_bluetooth_required),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.open_bluetooth),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.scan_barcode),
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }

    private fun printBluetooth() {
        val currentPrinterIp = prefs.getString(PRINTER_IP, null)
        if (currentPrinterIp == null) {
            startForResult.launch(
                Intent(
                    requireActivity(),
                    ScanningActivity::class.java
                )
            )
        } else {
            viewModel.setupPrinter(currentPrinterIp)
            binding.btnPrintBluetooth.isEnabled = false
            binding.btnPrintPDF.isEnabled = false
            binding.etxtBarcode.isEnabled = false
        }

        if (Printooth.hasPairedPrinter()) {
            viewModel.printItem(currentItem!!)
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                prefs.putString(PRINTER_IP, Printooth.getPairedPrinter()?.address)

            }
        }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ItemUiState.Empty -> {
                            binding.progressBlue.visibility = View.GONE
                            binding.cardItem.visibility = View.GONE
                            currentItem = null
                        }
                        is ItemUiState.Loading -> {
                            if (state.loading) {
                                binding.progressBlue.visibility = View.VISIBLE
                            } else {
                                binding.progressBlue.visibility = View.GONE
                            }
                        }
                        is ItemUiState.Loaded -> {
                            binding.progressBlue.visibility = View.GONE
                            binding.cardItem.visibility = View.VISIBLE
                            currentItem = state.itemState
                            with(state.itemState) {
                                binding.pdfLayout.txtBarcode.text = barcode
                                binding.pdfLayout.txtArea.text = area
                                binding.pdfLayout.txtGovernment.text = government
                                binding.pdfLayout.txtPhone.text = phoneNumber
                                binding.pdfLayout.txtStore.text = store
                                binding.textInputBarcode.isErrorEnabled = false
                                binding.pdfLayout.imgBarcode.setImageBitmap(barcode.toBarcodeBitmap());
                            }

                        }
                        else -> {
                            binding.progressBlue.visibility = View.GONE
                            binding.cardItem.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun initEffectObservation() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiEffects.collectLatest { effect ->
                when (effect) {
                    is ItemSideEffects.Error -> {
                        binding.progressBlue.visibility = View.GONE
                        binding.cardItem.visibility = View.GONE
                        binding.textInputBarcode.isErrorEnabled = true
                        binding.textInputBarcode.error = getString(effect.message)
                        currentItem = null
                    }
                    is ItemSideEffects.PrinterState -> {
                        binding.btnPrintBluetooth.isEnabled = true
                        binding.btnPrintPDF.isEnabled = true
                        binding.etxtBarcode.isEnabled = true
                        binding.progressBlue.visibility = View.GONE
                        binding.textInputBarcode.isErrorEnabled = false
                        Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun print(file: File) {
        val manager = requireActivity().getSystemService(Context.PRINT_SERVICE) as PrintManager

        val adapter = PDFDocumentAdapter(file)
        val attributes = PrintAttributes.Builder().build()
        manager.print("Document", adapter, attributes)
    }

    private fun hasConnectBluetooth() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasAccessAresLocation() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasScanBluetooth() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        var permissionsToReques = mutableListOf<String>()
        if (!hasScanBluetooth()) {
            permissionsToReques.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (!hasConnectBluetooth()) {
            permissionsToReques.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (!hasAccessAresLocation()) {
            permissionsToReques.add(Manifest.permission.ACCESS_FINE_LOCATION)

        }
        if (permissionsToReques.isNotEmpty()) {
            requestPermission.launch(permissionsToReques.toTypedArray())
        }
    }
}


fun String.toBarcodeBitmap(): Bitmap {
    var bitMatrix = MultiFormatWriter().encode(toString(), BarcodeFormat.CODE_128, 500, 50);
    var barcodeEncoder = BarcodeEncoder();
    return barcodeEncoder.createBitmap(bitMatrix);
}

fun isBluetoothEnabled(): Boolean {
    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    return mBluetoothAdapter.isEnabled
}