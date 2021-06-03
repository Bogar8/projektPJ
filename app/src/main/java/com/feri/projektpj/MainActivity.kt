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
import com.example.data.Mailbox
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
    val MY_INTERNET_PERMISSION_REQUEST = 112
    private var apiPackage: String? = ""
    private var app: ApplicationMy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        app = application as ApplicationMy
        if (app?.getLogin() == true)
            finish()
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this@MainActivity, MenuActivity::class.java)
        if (app?.getLogin() == true)
            startActivity(intent)
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_INTERNET_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, getString(R.string.internet_access_granted), Toast.LENGTH_LONG)
                .show()
        else
            Toast.makeText(this, getString(R.string.internet_access_error), Toast.LENGTH_LONG)
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
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
                            app?.setIsLogin(true)
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
                                                Mailbox(
                                                    mailboxes.getJSONObject(i).get("location")
                                                        .toString(),
                                                    mailboxes.getJSONObject(i).get("code")
                                                        .toString()
                                                )
                                            )
                                            Log.i(
                                                TAG,
                                                mailboxes.getJSONObject(i).get("code").toString()
                                            )
                                        }
                                    }
                                    val intent = Intent(this@MainActivity, MenuActivity::class.java)
                                    startActivity(intent)
                                }
                            })
                        }

                        if(!app?.getUsername().isNullOrEmpty())
                            makeToast(jsonObject.getString("message") + "\nHello " + app?.getUsername())
                        else
                            makeToast(jsonObject.getString("message"))
                    }
                }
            })
        }
    }

    fun makeToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun loginCamera(view: View) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        checkPermission()
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    fun loginWithUsernameAndPassword(view: View) {
        val i = Intent(this@MainActivity, LoginWithUsernameActivity::class.java)
        startActivityForResult(i, LoginWithUsernameActivity().ACTIVITY_ID)
    }

    fun exitApp(view: View) {
        finishAffinity()
    }
}