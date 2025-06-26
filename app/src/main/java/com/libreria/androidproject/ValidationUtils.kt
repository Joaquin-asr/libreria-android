package com.libreria.androidproject

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