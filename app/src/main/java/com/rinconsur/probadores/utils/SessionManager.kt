package com.rinconsur.probadores.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "rincon_sur_session"
    private const val KEY_GUEST_NAME = "active_guest_name"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Guarda el nombre del cliente/invitado activo en SharedPreferences.
     */
    fun guardarUltimoInvitado(context: Context, nombre: String) {
        getPreferences(context).edit().putString(KEY_GUEST_NAME, nombre.trim()).apply()
    }

    /**
     * Obtiene el nombre del último cliente/invitado guardado en el dispositivo.
     */
    fun obtenerUltimoInvitado(context: Context): String? {
        val nombre = getPreferences(context).getString(KEY_GUEST_NAME, null)
        return if (!nombre.isNullOrBlank()) nombre.trim() else null
    }

    /**
     * Limpia la sesión guardada del invitado en el dispositivo.
     */
    fun limpiarUltimoInvitado(context: Context) {
        getPreferences(context).edit().remove(KEY_GUEST_NAME).apply()
    }
}
