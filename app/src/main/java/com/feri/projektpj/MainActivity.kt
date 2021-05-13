package com.feri.projektpj

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG: String = com.feri.projektpj.MainActivity::class.java.getSimpleName()
    val ACTIVITY_ID = 100

    val MY_INTERNET_PERMISSION_REQUEST = 112
    private val client = OkHttpClient()
    private var apiPackage: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), MY_INTERNET_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == MY_INTERNET_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, getString(R.string.internet_access_granted), Toast.LENGTH_LONG).show()
        //codeScanner.startPreview()
        else
            Toast.makeText(this, getString(R.string.internet_access_error), Toast.LENGTH_LONG).show()
    }

    fun openScanCodeActivityForResult(view: View) {
        val intent = Intent(this@MainActivity, ScanActivity::class.java)
        startActivityForResult(intent, ScanActivity().ACTIVITY_ID)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanActivity().ACTIVITY_ID) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    var mailboxId = data!!.extras!![ScanActivity().MAILBOX_ID].toString()
                    Toast.makeText(this, getString(R.string.scan_successful), Toast.LENGTH_LONG)
                        .show()
                    apiGetToken(mailboxId)
                } else {
                    Toast.makeText(this, getString(R.string.no_values_error), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun apiGetToken(mailboxId: String) {
        val request = Request.Builder()
            .url("http://api-test.direct4.me/Sandbox/PublicAccess/V1/api/access/OpenBox?boxID=${mailboxId}&tokenFormat=2")
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, ex: IOException) {
                Log.i(TAG, ex.printStackTrace().toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    apiPackage = it.body()?.string()
                    Log.i(TAG, "Server response:" + apiPackage)
                }
            }
        })
    }
}