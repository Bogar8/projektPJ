package com.feri.projektpj

import android.app.Application
import android.widget.Toast
import com.example.data.Mailbox

class ApplicationMy : Application() {
    companion object {
        private var userID: String? = null
        private var username: String? = null
        private var userEmail: String? = null
        private var userMailboxes: ArrayList<Mailbox> = ArrayList()
    }

    fun getUserId(): String? {
        return userID
    }

    fun setUserId(id: String) {
        userID = id
    }

    fun getUsername(): String? {
        return username
    }

    fun setUsername(name: String) {
        username = name
    }

    fun getUserEmail(): String? {
        return userEmail
    }

    fun setUserEmail(mail: String) {
        userEmail = mail
    }

    fun addMailbox(mailbox: Mailbox) {
        userMailboxes.add(mailbox)
    }

    fun getMailboxes() : ArrayList<Mailbox>{
        return userMailboxes
    }

    fun getMailboxAtPosition(position: Int):Mailbox{
        return userMailboxes.get(position)
    }

    fun getMailboxesCount(): Int{
        return userMailboxes.size
    }

    override fun onCreate() {
        super.onCreate()
    }

}