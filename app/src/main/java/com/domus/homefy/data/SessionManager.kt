package com.domus.homefy.data

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    fun isLogged(): Boolean {
        return preferences.getBoolean("logged", false)
    }

    fun setLogged(value: Boolean) {
       preferences.edit().putBoolean("logged", value).apply()
    }

    fun toggleLogged() {
        val logged = !isLogged()
        setLogged(logged)
    }
}