package com.feri.projektpj

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.Optional.empty

class AddAccessActivity : AppCompatActivity() {
    private val TAG: String = this::class.java.simpleName
    val ACTIVITY_ID = 104
    val MAILBOX_CODE = "MAILBOX_CODE"

    private var etUsername: EditText? = null
    private var tvDateFrom: TextView? = null
    private var tvDateTo: TextView? = null
    private var tvTimeFrom: TextView? = null
    private var tvTimeTo: TextView? = null
    private var YearFrom: Int = 0
    private var MonthFrom: Int = 0
    private var DayFrom: Int = 0
    private var HourFrom: Int = 0
    private var MinuteFrom: Int = 0
    private var YearTo: Int = 0
    private var MonthTo: Int = 0
    private var DayTo: Int = 0
    private var HourTo: Int = 0
    private var MinuteTo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_access)
        etUsername = findViewById(R.id.etUsername)
        tvDateFrom = findViewById(R.id.tvDateFrom)
        tvDateTo = findViewById(R.id.tvDateTo)
        tvTimeFrom = findViewById(R.id.tvTimeFrom)
        tvTimeTo = findViewById(R.id.tvTimeTo)
    }

    fun onClickSelectDateAndTime(v: View) {
        //Opens date picker dialog for event date input
        if (v === tvDateFrom) {
            val c = Calendar.getInstance()
            YearFrom = c[Calendar.YEAR]
            MonthFrom = c[Calendar.MONTH]
            DayFrom = c[Calendar.DAY_OF_MONTH]
            var tvDateFrom: TextView = findViewById(R.id.tvDateTo)
            val datePickerDialog = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth -> tvDateFrom.setText(dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year) },
                YearFrom,
                MonthFrom,
                DayFrom
            )
            datePickerDialog.show()
        }
        if (v === tvDateTo) {
            val c = Calendar.getInstance()
            YearFrom = c[Calendar.YEAR]
            MonthFrom = c[Calendar.MONTH]
            DayFrom = c[Calendar.DAY_OF_MONTH]
            var tvDateTo: TextView = findViewById(R.id.tvDateTo)
            val datePickerDialog = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth -> tvDateTo.setText(dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year) },
                YearTo,
                MonthTo,
                DayTo
            )
            datePickerDialog.show()
        }
        //Opens date picker dialog for event time input
        if (v === tvTimeFrom) {
            val c = Calendar.getInstance()
            HourFrom = c[Calendar.HOUR_OF_DAY]
            MinuteFrom = c[Calendar.MINUTE]
            var tvTimeFrom: TextView = findViewById(R.id.tvTimeFrom)
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                { view, hourOfDay, minute -> tvTimeFrom.setText(hourOfDay.toString() + ":" + minute) },
                HourFrom,
                MinuteFrom,
                true
            )
            timePickerDialog.show()
        }
        if (v === tvTimeTo) {
            val c = Calendar.getInstance()
            HourFrom = c[Calendar.HOUR_OF_DAY]
            MinuteFrom = c[Calendar.MINUTE]
            var tvTimeTo: TextView = findViewById(R.id.tvTimeTo)
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                { view, hourOfDay, minute -> tvTimeTo.setText(hourOfDay.toString() + ":" + minute) },
                HourTo,
                MinuteTo,
                true
            )
            timePickerDialog.show()
        }
    }

    fun onClickAccess(view: View){
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
}