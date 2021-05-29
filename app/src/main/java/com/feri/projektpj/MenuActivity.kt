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
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


class MenuActivity : AppCompatActivity() {
    private val TAG: String = com.feri.projektpj.MenuActivity::class.java.simpleName
    val CAMERA_REQUEST = 1001
    private var apiPackage: String? = ""
    private var app: ApplicationMy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        app = application as ApplicationMy
    }

    fun addPictureClick(view: View) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
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
                    }
                }
            })
        }
    }
}