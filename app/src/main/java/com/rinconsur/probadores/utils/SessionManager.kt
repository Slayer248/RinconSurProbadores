package com.rinconsur.probadores.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "rincon_sur_session"
    private const val KEY_GUEST_NAME = "active_guest_name"
    private const val KEY_USER_ID = "logged_user_id"
    private const val KEY_USERNAME = "logged_username"
    private const val KEY_USER_ROL = "logged_user_rol"
    private const val KEY_DEBE_CAMBIAR_CLAVE = "must_change_password"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // --- SESIÓN EMPLEADOS ---
    fun guardarSesionEmpleado(
        context: Context,
        id: Long,
        username: String,
        rol: String,
        debeCambiarContrasena: Boolean = false
    ) {
        getPreferences(context).edit()
            .putLong(KEY_USER_ID, id)
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_USER_ROL, rol)
            .putBoolean(KEY_DEBE_CAMBIAR_CLAVE, debeCambiarContrasena)
            .apply()
    }

    fun debeCambiarContrasena(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DEBE_CAMBIAR_CLAVE, false)
    }

    fun marcarContrasenaCambiada(context: Context) {
        getPreferences(context).edit().putBoolean(KEY_DEBE_CAMBIAR_CLAVE, false).apply()
    }

    fun obtenerEmpleadoId(context: Context): Long {
        return getPreferences(context).getLong(KEY_USER_ID, -1L)
    }

    fun obtenerEmpleadoUsername(context: Context): String? {
        return getPreferences(context).getString(KEY_USERNAME, null)
    }

    fun obtenerEmpleadoRol(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_ROL, null)
    }

    fun cerrarSesionEmpleado(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_USER_ROL)
            .remove(KEY_DEBE_CAMBIAR_CLAVE)
            .apply()
    }

    // --- SESIÓN INVITADOS ---
    fun guardarUltimoInvitado(context: Context, nombre: String) {
        getPreferences(context).edit().putString(KEY_GUEST_NAME, nombre.trim()).apply()
    }

    fun obtenerUltimoInvitado(context: Context): String? {
        val nombre = getPreferences(context).getString(KEY_GUEST_NAME, null)
        return if (!nombre.isNullOrBlank()) nombre.trim() else null
    }

    fun limpiarUltimoInvitado(context: Context) {
        getPreferences(context).edit().remove(KEY_GUEST_NAME).apply()
    }
}