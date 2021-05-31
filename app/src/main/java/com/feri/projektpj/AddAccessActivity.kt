package com.feri.projektpj

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.data.Mailbox
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class AddAccessActivity : AppCompatActivity() {
    private val TAG: String = this::class.java.simpleName
    val ACTIVITY_ID = 104
    val MAILBOX_CODE = "MAILBOX_CODE"
    var mailboxCode: String? = null
    private var apiPackage: String? = ""
    private var etUsername: EditText? = null
    private var tvDateFrom: TextView? = null
    private var tvDateTo: TextView? = null
    private var tvTimeFrom: TextView? = null
    private var tvTimeTo: TextView? = null
    private var yearFrom: Int = 0
    private var monthFrom: Int = 0
    private var dayFrom: Int = 0
    private var hourFrom: Int = 0
    private var minuteFrom: Int = 0
    private var yearTo: Int = 0
    private var monthTo: Int = 0
    private var dayTo: Int = 0
    private var hourTo: Int = 0
    private var minuteTo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_access)
        etUsername = findViewById(R.id.etUsername)
        tvDateFrom = findViewById(R.id.tvDateFrom)
        tvDateTo = findViewById(R.id.tvDateTo)
        tvTimeFrom = findViewById(R.id.tvTimeFrom)
        tvTimeTo = findViewById(R.id.tvTimeTo)
        setCodeFromIntent()
    }

    fun setCodeFromIntent(){
        val extras = intent.extras
        if (extras != null) {
            mailboxCode = extras.getString(MAILBOX_CODE)
        }
    }

    fun onClickSelectDateAndTime(v: View) {
        //Opens date picker dialog for event date input
        if (v === tvDateFrom) {
            val c = Calendar.getInstance()
            yearFrom = c[Calendar.YEAR]
            monthFrom = c[Calendar.MONTH]
            dayFrom = c[Calendar.DAY_OF_MONTH]
            var tvDateFrom: TextView = findViewById(R.id.tvDateTo)
            val datePickerDialog = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth -> tvDateFrom.setText(dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year) },
                yearFrom,
                monthFrom,
                dayFrom
            )
            datePickerDialog.show()
        }
        if (v === tvDateTo) {
            val c = Calendar.getInstance()
            yearFrom = c[Calendar.YEAR]
            monthFrom = c[Calendar.MONTH]
            dayFrom = c[Calendar.DAY_OF_MONTH]
            var tvDateTo: TextView = findViewById(R.id.tvDateTo)
            val datePickerDialog = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth -> tvDateTo.setText(dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year) },
                yearTo,
                monthTo,
                dayTo
            )
            datePickerDialog.show()
        }
        //Opens date picker dialog for event time input
        if (v === tvTimeFrom) {
            val c = Calendar.getInstance()
            hourFrom = c[Calendar.HOUR_OF_DAY]
            minuteFrom = c[Calendar.MINUTE]
            var tvTimeFrom: TextView = findViewById(R.id.tvTimeFrom)
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                { view, hourOfDay, minute -> tvTimeFrom.setText(hourOfDay.toString() + ":" + minute) },
                hourFrom,
                minuteFrom,
                true
            )
            timePickerDialog.show()
        }
        if (v === tvTimeTo) {
            val c = Calendar.getInstance()
            hourFrom = c[Calendar.HOUR_OF_DAY]
            minuteFrom = c[Calendar.MINUTE]
            var tvTimeTo: TextView = findViewById(R.id.tvTimeTo)
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                { view, hourOfDay, minute -> tvTimeTo.setText(hourOfDay.toString() + ":" + minute) },
                hourTo,
                minuteTo,
                true
            )
            timePickerDialog.show()
        }
    }

    fun onClickAddAccess(view: View){
        try {
            if (TextUtils.isEmpty(etUsername?.getText())) {
                etUsername?.setError(getString(R.string.empty))
                return
            }
            if (TextUtils.isEmpty(tvDateFrom?.getText())) {
                tvDateFrom?.setError(getString(R.string.empty))
                return
            }
            if (TextUtils.isEmpty(tvDateTo?.getText())) {
                tvDateTo?.setError(getString(R.string.empty))
                return
            }
            if (TextUtils.isEmpty(tvTimeFrom?.getText())) {
                tvTimeFrom?.setError(getString(R.string.empty))
                return
            }
            if (TextUtils.isEmpty(tvTimeTo?.getText())) {
                tvTimeTo?.setError(getString(R.string.empty))
                return
            }
            val calendarFrom = Calendar.getInstance()
            val calendarTo = Calendar.getInstance()

            calendarFrom.set(Calendar.YEAR, yearFrom)
            calendarFrom.set(Calendar.MONTH, monthFrom)
            calendarFrom.set(Calendar.DAY_OF_MONTH, dayFrom)
            calendarFrom.set(Calendar.MINUTE, minuteFrom)
            calendarFrom.set(Calendar.HOUR_OF_DAY, hourFrom)

            calendarTo.set(Calendar.YEAR, yearTo)
            calendarTo.set(Calendar.MONTH, monthTo)
            calendarTo.set(Calendar.DAY_OF_MONTH, dayTo)
            calendarTo.set(Calendar.MINUTE, minuteTo)
            calendarTo.set(Calendar.HOUR_OF_DAY, hourTo)

            addAccess(mailboxCode, calendarFrom.time.toString(), calendarTo.time.toString(), etUsername?.text.toString())
            etUsername?.setText("")
            tvDateFrom?.setText(R.string.date_from)
            tvDateTo?.setText(R.string.date_to)
            tvTimeFrom?.setText(getString(R.string.time_from))
            tvTimeTo?.setText(R.string.time_to)
            finish()
        }catch (ex: Exception){
            Toast.makeText(this, "check inserted values!", Toast.LENGTH_SHORT).show()
        }
    }

    fun addAccess(code: String?, dateFrom: String, dateTo: String, username: String) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("mailbox_code", code)
            .add("date_from", dateFrom)
            .add("date_to", dateTo)
            .build()
        val request: Request = Request.Builder()
            .url("http://10.0.2.2:3000/packageAccess/api/create")
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
                        Log.i(TAG, "Dostop dodan")
                    }
                }
            }
        })

    }
}