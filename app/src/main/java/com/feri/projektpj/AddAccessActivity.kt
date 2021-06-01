package com.feri.projektpj

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class AddAccessActivity : AppCompatActivity() {
    private val TAG: String = this::class.java.simpleName
    val ACTIVITY_ID = 104
    val MAILBOX_CODE = "MAILBOX_CODE"
    var mailboxCode: String? = null
    private var app: ApplicationMy? = null

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
        app = ApplicationMy()
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
            var tvDateFrom: TextView = findViewById(R.id.tvDateFrom)
            val dateSetListener = DatePickerDialog.OnDateSetListener{ timePicker, year, monthOfYear, dayOfMonth ->
                yearFrom = year
                monthFrom = monthOfYear
                dayFrom = dayOfMonth
                tvDateFrom.setText(dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year)
            }
            val datePickerDialog = DatePickerDialog(this,
                dateSetListener,
                yearFrom,
                monthFrom,
                dayFrom
            )
            datePickerDialog.show()
        }
        if (v === tvDateTo) {
            val c = Calendar.getInstance()
            yearTo = c[Calendar.YEAR]
            monthTo = c[Calendar.MONTH]
            dayTo = c[Calendar.DAY_OF_MONTH]
            var tvDateTo: TextView = findViewById(R.id.tvDateTo)
            val dateSetListener = DatePickerDialog.OnDateSetListener{ timePicker, year, monthOfYear, dayOfMonth ->
                yearTo = year
                monthTo = monthOfYear
                dayTo = dayOfMonth
                tvDateTo.setText(dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year)
            }
            val datePickerDialog = DatePickerDialog(this,
                dateSetListener,
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

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                hourFrom = hour
                minuteFrom = minute
                tvTimeFrom.setText(hour.toString() + ":" + minute)
            }
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                timeSetListener,
                hourFrom,
                minuteFrom,
                true
            )
            timePickerDialog.show()
        }
        if (v === tvTimeTo) {
            val c = Calendar.getInstance()
            hourTo = c[Calendar.HOUR_OF_DAY]
            minuteTo = c[Calendar.MINUTE]
            var tvTimeTo: TextView = findViewById(R.id.tvTimeTo)
            // Launch Time Picker Dialog
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                hourTo = hour
                minuteTo = minute
                tvTimeTo.setText(hour.toString() + ":" + minute)
            }
            val timePickerDialog = TimePickerDialog(this,
                timeSetListener,
                hourTo,
                minuteTo,
                true
            )
            timePickerDialog.show()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
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

            val tz = TimeZone.getTimeZone("UTC")
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz)
            addAccess(mailboxCode, df.format(Date(yearFrom-1900, monthFrom, dayFrom, hourFrom, minuteFrom)), df.format(Date(yearTo-1900, monthTo, dayTo, hourTo, minuteTo)), etUsername?.text.toString())
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
            .add("user_id", app?.getUserId())
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

private fun Date.format(isoLocalDate: DateTimeFormatter?) {

}
