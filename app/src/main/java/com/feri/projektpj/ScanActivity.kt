package com.feri.projektpj

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*

class ScanActivity : AppCompatActivity() {
    val ACTIVITY_ID = 101

    val MY_CAMERA_PERMISSION_REQUEST = 111

    val MAILBOX_ID: String = "mailbox_id"
    private lateinit var mailboxId: String
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView:CodeScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        scannerView = findViewById<CodeScannerView>(R.id.scannView)
        codeScanner = CodeScanner(this, scannerView)

        checkPermission()
    }

    fun scanQR(view: View) {

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        // Check permission
        checkPermission()

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                // saves scaned mailbox id
                mailboxId = it.text;
                returnValue(mailboxId);
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        codeScanner.startPreview()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == MY_CAMERA_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, getString(R.string.camera_access_granted), Toast.LENGTH_LONG).show()
        //codeScanner.startPreview()
        else
            Toast.makeText(this, getString(R.string.camera_access_error), Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun returnValue(mailboxId: String) {
        val data = intent
        data.putExtra(MAILBOX_ID, mailboxId)
        setResult(RESULT_OK, data)
        finish()
    }
}