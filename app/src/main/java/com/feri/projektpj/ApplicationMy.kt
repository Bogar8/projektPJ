package com.feri.projektpj

import android.app.Application

class ApplicationMy : Application() {

    companion object {
        private var userID: String? = null
        private var username: String? = null
        private var userEmail: String? = null
        private var userMailboxes: ArrayList<String> = ArrayList()
    }

    public fun getUserId(): String? {
        return userID
    }

    public fun setUserId(id: String) {
        userID = id
    }

    public fun getUsername(): String? {
        return username
    }

    public fun setUsername(name: String) {
        username = name
    }

    public fun getUserEmail(): String? {
        return userEmail
    }

    public fun setUserEmail(mail: String) {
        userEmail = mail
    }

    public fun addMailbox(koda: String) {
        userMailboxes.add(koda)
    }

    public fun getMailboxes() : ArrayList<String>{
        return userMailboxes
    }

    override fun onCreate() {
        super.onCreate()
    }

}