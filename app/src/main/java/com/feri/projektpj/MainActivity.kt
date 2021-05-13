package com.feri.projektpj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    val ACTIVITY_ID = 100

    private lateinit var mailboxId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openScanCodeActivityForResult(view: View) {
        val intent = Intent(this@MainActivity, ScanActivity::class.java)
        startActivityForResult(intent, ScanActivity().ACTIVITY_ID)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanActivity().ACTIVITY_ID)
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    mailboxId = data!!.extras!![ScanActivity().MAILBOX_ID].toString()
                    Toast.makeText(this, getString(R.string.scan_successful), Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this, getString(R.string.no_values_error), Toast.LENGTH_LONG).show()
                }
            }
    }
}