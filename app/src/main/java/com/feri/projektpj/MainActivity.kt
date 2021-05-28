package com.feri.projektpj

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.zip.ZipFile


class MainActivity : AppCompatActivity() {
    private val TAG: String = com.feri.projektpj.MainActivity::class.java.simpleName
    val ACTIVITY_ID = 100
    var mMediaPlayer: MediaPlayer? = null
    val MY_INTERNET_PERMISSION_REQUEST = 112
    private val client = OkHttpClient()
    private var apiPackage: String? = ""
    private var app: ApplicationMy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        app = application as ApplicationMy
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET),
                MY_INTERNET_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_INTERNET_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, getString(R.string.internet_access_granted), Toast.LENGTH_LONG)
                .show()
        //codeScanner.startPreview()
        else
            Toast.makeText(this, getString(R.string.internet_access_error), Toast.LENGTH_LONG)
                .show()
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
                    var mailboxId = data.extras!![ScanActivity().MAILBOX_ID].toString()
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
        var id = mailboxId.substringAfter("/").substringBefore("/")
        val request = Request.Builder()
            .url("https://api-test.direct4.me/Sandbox/PublicAccess/V1/api/access/OpenBox?boxID=${id}&tokenFormat=2")
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, ex: IOException) {
                Log.i(TAG, ex.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    apiPackage = it.body()?.string()
                    val jsonObject = JSONObject(apiPackage)
                    val data = jsonObject.get("Data") //pridobimo string iz odgovora
                    val decodedBytes = Base64.decode(
                        data.toString(),
                        Base64.DEFAULT
                    ) //dekodiramo string da dobimo zip
                    val path = filesDir //pot do mape z datotekami
                    val zipFIle = File.createTempFile("unlockSound", ".zip", path) //shranimo zip
                    val os = FileOutputStream(zipFIle)
                    os.write(decodedBytes)
                    os.close()
                    if (zipFIle.exists())
                        unzip(zipFIle, path.toString()) //razpakiramo zip
                    val token =
                        File(path.toString() + "/token.wav") //pridobimo token za predvajanje
                    if (token.exists()) {
                        playSound(token.path)
                        token.delete()
                    }
                    if (zipFIle.exists())
                        zipFIle.delete()
                }
            }
        })
    }


    fun playSound(path: String) {
        mMediaPlayer = MediaPlayer()
        var count = 0
        mMediaPlayer!!.setOnCompletionListener(object : OnCompletionListener {
            var maxCount = 5
            override fun onCompletion(mediaPlayer: MediaPlayer) {
                if (count < maxCount) {
                    count++
                    mediaPlayer.seekTo(0)
                    mediaPlayer.start()
                }
            }
        })

        mMediaPlayer!!.setDataSource(path)
        mMediaPlayer!!.prepare()
        mMediaPlayer!!.start()
    }


    fun unzip(zipFilePath: File, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory + File.separator + entry.name
                    if (!entry.isDirectory) {
                        // if the entry is a file, extracts it
                        extractFile(input, filePath)
                    } else {
                        // if the entry is a directory, make the directory
                        val dir = File(filePath)
                        dir.mkdir()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(4096)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}