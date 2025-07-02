package com.libreria.androidproject.model

import com.google.firebase.Timestamp

data class Libro(
    var cod: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fchpub: Timestamp? = null,
    var precio: Double = 0.0,
    var stock: Int = 0,
    var autor: String = "",
    var portadaUri: String = "",
    var portadaNombre: String = ""

) {
    fun toMap(): Map<String, Any> = mapOf(
        "titulo" to titulo,
        "descripcion" to descripcion,
        "fchpub" to (fchpub ?: Timestamp.now()),
        "precio" to precio,
        "stock" to stock,
        "autor" to autor,
        "portadaUri" to portadaUri,
        "portadaNombre"  to portadaNombre
    )
}