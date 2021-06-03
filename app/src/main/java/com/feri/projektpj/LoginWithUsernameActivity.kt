package com.feri.projektpj

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.data.Mailbox
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginWithUsernameActivity : AppCompatActivity() {
    private val TAG: String = com.feri.projektpj.MainActivity::class.java.simpleName
    val ACTIVITY_ID = 102

    private var etUsername: EditText? = null
    private var etPassword: EditText? = null
    private var etEmail: EditText? = null
    private var apiPackage: String? = ""
    private var app: ApplicationMy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_username)
        etUsername = findViewById(R.id.etPersonName)
        etPassword = findViewById(R.id.etPassword)
        etEmail = findViewById(R.id.etPersonEmail)
        app = application as ApplicationMy
        if (app?.getLogin() == true)
            finish()
        if (intent.extras!!.getString("MODE") == "REGISTER")
            findViewById<Button>(R.id.btnLogin).text = "Register"
        else
            etEmail?.visibility = View.INVISIBLE

    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this@LoginWithUsernameActivity, MenuActivity::class.java)
        if (app?.getLogin() == true)
            startActivity(intent)
    }

    fun buttonClick(view: View) {
        if (intent.extras!!.getString("MODE") == "REGISTER")
            register()
        else
            login()
    }

    fun login(){
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
                            app?.setIsLogin(true)
                            Log.i(
                                TAG,
                                "${app?.getUserEmail()}  ${app?.getUsername()}  ${app?.getUserId()}"
                            )
                            val bodyMailboxes =
                                FormBody.Builder().add("userId", app?.getUserId().toString())
                                    .build()
                            val requestMailboxes: Request = Request.Builder()
                                .url("http://10.0.2.2:3000/mailbox/api/myMailboxes")
                                .post(bodyMailboxes).build()
                            client.newCall(requestMailboxes).enqueue(object : Callback {
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
                                    val intent = Intent(
                                        this@LoginWithUsernameActivity,
                                        MenuActivity::class.java
                                    )
                                    startActivity(intent)
                                }
                            })
                        }
                        makeToast(jsonObject.getString("message"))
                    }
                }
            })
        } else {
            Toast.makeText(this, "Fill all boxes", Toast.LENGTH_LONG)
        }
    }

    fun register(){
            val username = etUsername?.text.toString()
            val password = etPassword?.text.toString()
            val email = etEmail?.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                val formBody = FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("email", email)
                    .build()
                val request: Request = Request.Builder()
                    .url("http://10.0.2.2:3000/users/api/create")
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
                            makeToast(jsonObject.getString("message"))
                           finish()
                        }
                    }
                })
            } else {
                makeToast("Fill all boxes")
            }
    }

    fun makeToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                this@LoginWithUsernameActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}