package com.feri.projektpj

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.data.Mailbox
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile
import kotlin.jvm.Throws


class MenuActivity : AppCompatActivity() {
    private val TAG: String = com.feri.projektpj.MenuActivity::class.java.simpleName
    val CAMERA_REQUEST = 1001
    private var apiPackage: String? = ""
    private var app: ApplicationMy? = null
    var mMediaPlayer: MediaPlayer? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        app = application as ApplicationMy
    }

    fun addPictureClick(view: View) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    fun openScanCodeActivityForResult(view: View) {
        val intent = Intent(this@MenuActivity, ScanActivity::class.java)
        startActivityForResult(intent, ScanActivity().ACTIVITY_ID)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanActivity().ACTIVITY_ID) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    var mailboxId = data.extras!![ScanActivity().MAILBOX_ID].toString()
                    mailboxId = mailboxId.substringAfter("/").substringBefore("/")
                    Toast.makeText(this, getString(R.string.scan_successful), Toast.LENGTH_LONG)
                        .show()

                    val formBody = FormBody.Builder()
                        .add("user_id", app?.getUserId().toString())
                        .add("mailbox_code", mailboxId)
                        .build()
                    val request: Request = Request.Builder()
                        .url("http://10.0.2.2:3000/packageaccess/api/access")
                        .post(formBody)
                        .build()
                    val client = OkHttpClient()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, ex: IOException) {
                            Log.i(TAG, ex.message.toString())
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                apiPackage = it.body()?.string()
                                val jsonObject = JSONObject(apiPackage)
                                if (jsonObject.getBoolean("successful")) {
                                    apiGetToken(mailboxId)
                                    Log.i(
                                        TAG,
                                        "${app?.getUserEmail()}  ${app?.getUsername()}  ${app?.getUserId()}"
                                    )
                                }
                                makeToast(jsonObject.getString("message"))
                            }
                        }
                    })
                } else {
                    Toast.makeText(this, getString(R.string.no_values_error), Toast.LENGTH_LONG)
                        .show()
                }
            }
        } else
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                val photo = data?.extras?.get("data") as Bitmap
                val stream = ByteArrayOutputStream()
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                val formBody = FormBody.Builder()
                    .add("photo", encodedImage)
                    .add("user_id", app?.getUserId().toString())
                    .build()
                val request: Request = Request.Builder()
                    .url("http://10.0.2.2:3000/face/api/add")
                    .post(formBody)
                    .build()
                val client = OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, ex: IOException) {
                        Log.i(TAG, ex.message.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            apiPackage = it.body()?.string()
                            val jsonObject = JSONObject(apiPackage)
                            Log.i(TAG, jsonObject.get("message").toString())
                            makeToast(jsonObject.getString("message"))
                        }
                    }
                })
            }
    }

    fun apiGetToken(mailboxId: String) {
        val request = Request.Builder()
            .url("https://api-test.direct4.me/Sandbox/PublicAccess/V1/api/access/OpenBox?boxID=${mailboxId}&tokenFormat=2")
            .post(
                RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    ""
                )
            )
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
                    val zipFIle = File.createTempFile(
                        "unlockSound",
                        ".zip",
                        path
                    ) //shranimo zip
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
        mMediaPlayer!!.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
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

    fun openMyMailboxes(view: View) {
        val intent = Intent(this@MenuActivity, MyMailboxesActivity::class.java)
        startActivityForResult(intent, MyMailboxesActivity().ACTIVITY_ID)
    }

    fun makeToast(message: String){
        runOnUiThread {
            Toast.makeText(
                this@MenuActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}