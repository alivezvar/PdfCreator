package com.vezvarcode.pdfcreator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vezvarcode.pdfcreator.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "MainActivityTAG"
    }

    private lateinit var binding: ActivityMainBinding


    private lateinit var requestExportPdf: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        permissions()
        listeners()



    }

    private fun permissions() {
        requestExportPdf = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (isPermissionsGranted(permissions)) {

                binding.txtText.post {
                    createPdf(binding.txtText , binding.txtText.width, binding.txtText.height)
                }

            } else {
                permissions.forEach {
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            it.key
                        )
                    if (!showRationale) {
                        openSettingForPermission()
                        return@registerForActivityResult
                    }
                }
            }
        }
    }

    private fun listeners() {
        binding.btnExport.setOnClickListener {
            requestExportPdf.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun createPdf(view : View , width: Int, height: Int) {


        val fileName = "myPdf"
        val folderPath = Environment.getExternalStorageDirectory().path.plus(File.separator).plus(getString(R.string.app_name))


        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(width, height, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas

        val bitmap = loadBitmapFromView(
            view,
            width,
            height
        )

        if (bitmap == null){
            Log.i(TAG, "Error creating bitmap from view !")
            toast("Error creating bitmap from view !")
            return
        }

        canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)


        var filePath = ""
        val folderFile = File(folderPath)
        try {
            folderFile.mkdirs()
            filePath = "$folderPath/$fileName.pdf"
            val pdfFile = File(filePath)
            document.writeTo(FileOutputStream(pdfFile))


            Log.i(TAG, "Pdf Created !")
            toast("Pdf Created !")

        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(TAG, "Error Create Pdf: ${e.message}")
            toast("Error Create Pdf. See Logs !")
        }

        document.close()
    }


    private fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap? {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }



    private fun isPermissionsGranted(
        permissions: MutableMap<String, Boolean>
    ): Boolean {
        permissions.forEach {
            if (!it.value) {
                return false
            }
        }
        return true
    }

    private fun openSettingForPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun toast(text : String){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}