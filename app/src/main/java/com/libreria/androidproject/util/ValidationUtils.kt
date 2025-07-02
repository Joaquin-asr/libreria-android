package com.libreria.androidproject.util

import android.widget.EditText

//Validacion para los campos
fun EditText.validateRequired(errorMsg: String = "Requerido"): Boolean {
    return if (text.isBlank()) {
        error = errorMsg
        false
    } else {
        error = null
        true
    }
}