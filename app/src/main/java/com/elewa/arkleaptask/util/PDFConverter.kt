package com.elewa.arkleaptask.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import com.elewa.arkleaptask.R
import com.elewa.arkleaptask.modules.scanner.presentation.ui.toBarcodeBitmap
import com.elewa.arkleaptask.modules.scanner.presentation.uimodel.ItemUiModel
import java.io.File
import java.io.FileOutputStream

class PDFConverter {

    private fun createBitmapFromView(
        context: Context,
        view: View,
        pdfDetails: ItemUiModel,
        activity: Activity
    ): Bitmap {
        val imgBarcode = view.findViewById<ImageView>(R.id.imgBarcode)
        val txtBarcode = view.findViewById<TextView>(R.id.txtBarcode)
        val txtGovernment = view.findViewById<TextView>(R.id.txtGovernment)
        val txtArea = view.findViewById<TextView>(R.id.txtArea)
        val txtStore = view.findViewById<TextView>(R.id.txtStore)
        val txtPhone = view.findViewById<TextView>(R.id.txtPhone)
        with(pdfDetails) {
            txtBarcode.text = barcode
            txtArea.text = area
            txtGovernment.text = government
            txtPhone.text = phoneNumber
            txtStore.text = store
            imgBarcode.setImageBitmap(barcode.toBarcodeBitmap());
        }
        return createBitmap(context, view, activity)
    }

    private fun createBitmap(
        context: Context,
        view: View,
        activity: Activity,
    ): Bitmap {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(displayMetrics)
            displayMetrics.densityDpi
        } else {
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
            )
        )
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return Bitmap.createScaledBitmap(bitmap, 595, 842, true)
    }

    private fun convertBitmapToPdf(bitmap: Bitmap, context: Context): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0F, 0F, null)
        pdfDocument.finishPage(page)
        val filePath = File(context.getExternalFilesDir(null), "bitmapPdf.pdf")
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()
        // to render pdf and open it in pdf view using device apps(pdf viewer)
//        renderPdf(context, filePath)
        return filePath
    }

    fun createPdf(
        context: Context,
        pdfDetails: ItemUiModel,
        activity: Activity
    ): File {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.pdf_item, null)

        val bitmap = createBitmapFromView(context, view, pdfDetails, activity)
        return convertBitmapToPdf(bitmap, activity)
    }


    private fun renderPdf(context: Context, filePath: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            filePath
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "application/pdf")

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {

        }
    }
}