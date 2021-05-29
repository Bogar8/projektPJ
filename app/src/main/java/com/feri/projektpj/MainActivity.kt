package com.feri.projektpj

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile


class MainActivity : AppCompatActivity() {
    private val TAG: String = com.feri.projektpj.MainActivity::class.java.simpleName
    val ACTIVITY_ID = 100
    val CAMERA_REQUEST = 1001
    var mMediaPlayer: MediaPlayer? = null
    val MY_INTERNET_PERMISSION_REQUEST = 112
    private val client = OkHttpClient()
    private var apiPackage: String? = ""
    private var app: ApplicationMy? = null
    private var etUsername: EditText? = null
    private var etPassword: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        app = application as ApplicationMy
        etUsername = findViewById(R.id.editTextTextPersonName)
        etPassword = findViewById(R.id.editTextTextPassword)
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
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            val stream = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
            val formBody = FormBody.Builder()
                .add("photo", encodedImage)
                .build()
            val request: Request = Request.Builder()
                .url("http://10.0.2.2:3000/face/api/unlock")
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
                        if (jsonObject.getBoolean("successful")) {
                            app?.setUserId(jsonObject.get("user_id").toString())
                            app?.setUsername(jsonObject.get("username").toString())
                            app?.setUserEmail(jsonObject.get("email").toString())
                            Log.i(
                                TAG,
                                "${app?.getUserEmail()}  ${app?.getUsername()}  ${app?.getUserId()}"
                            )
                            val bodyMailboxes = FormBody.Builder()
                                .add("userId", app?.getUserId().toString())
                                .build()
                            val requestMailboxes: Request = Request.Builder()
                                .url("http://10.0.2.2:3000/mailbox/api/myMailboxes")
                                .post(bodyMailboxes)
                                .build()
                            client.newCall(requestMailboxes).enqueue(object : Callback {
                                override fun onFailure(call: Call, ex: IOException) {
                                    Log.i(TAG, ex.message.toString())
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    response.use {
                                        apiPackage = it.body()?.string()
                                        val jsonObject = JSONObject(apiPackage)
                                        val mailboxes = jsonObject.getJSONArray("mailboxes")
                                        for (i in 0 until mailboxes.length()) {
                                            app?.addMailbox(
                                                mailboxes.getJSONObject(i).get("code").toString()
                                            )
                                            Log.i(
                                                TAG,
                                                mailboxes.getJSONObject(i).get("code").toString()
                                            )
                                        }
                                    }
                                    // val intent = Intent(this@MainActivity, ScanActivity::class.java)
                                    //  startActivity(intent)

                                }
                            })
                        }
                    }
                }
            })
        }
    }

    fun apiGetToken(mailboxId: String) {
        var id = mailboxId.substringAfter("/").substringBefore("/")
        val request = Request.Builder()
            .url("https://api-test.direct4.me/Sandbox/PublicAccess/V1/api/access/OpenBox?boxID=${id}&tokenFormat=2")
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


    fun loginClick(view: View) {
        val username = etUsername?.text.toString()
        val password = etPassword?.text.toString()
        if (username.isNotEmpty() && password.isNotEmpty()) {
            val formBody = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build()
            val request: Request = Request.Builder()
                .url("http://10.0.2.2:3000/users/api/login")
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
                            app?.setUserId(jsonObject.get("userId").toString())
                            app?.setUsername(jsonObject.get("username").toString())
                            app?.setUserEmail(jsonObject.get("email").toString())
                            Log.i(
                                TAG,
                                "${app?.getUserEmail()}  ${app?.getUsername()}  ${app?.getUserId()}"
                            )
                            val bodyMailboxes = FormBody.Builder()
                                .add("userId", app?.getUserId().toString())
                                .build()
                            val requestMailboxes: Request = Request.Builder()
                                .url("http://10.0.2.2:3000/mailbox/api/myMailboxes")
                                .post(bodyMailboxes)
                                .build()
                            client.newCall(requestMailboxes)
                                .enqueue(object : Callback {
                                    override fun onFailure(
                                        call: Call,
                                        ex: IOException
                                    ) {
                                        Log.i(TAG, ex.message.toString())
                                    }

                                    override fun onResponse(
                                        call: Call,
                                        response: Response
                                    ) {
                                        response.use {
                                            apiPackage = it.body()?.string()
                                            val jsonObject = JSONObject(apiPackage)
                                            val mailboxes =
                                                jsonObject.getJSONArray("mailboxes")
                                            for (i in 0 until mailboxes.length()) {
                                                app?.addMailbox(
                                                    mailboxes.getJSONObject(i)
                                                        .get("code").toString()
                                                )
                                                Log.i(
                                                    TAG,
                                                    mailboxes.getJSONObject(i)
                                                        .get("code").toString()
                                                )
                                            }
                                        }
                                        // val intent = Intent(this@MainActivity, ScanActivity::class.java)
                                        //  startActivity(intent)
                                    }
                                })
                        }
                    }
                }
            })
        } else {
            makeToast("Fill all boxes")
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(
            baseContext,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    fun loginCamera(view: View) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        checkPermission()
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }


}