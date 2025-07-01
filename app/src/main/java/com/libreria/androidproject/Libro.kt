package com.libreria.androidproject


data class Libro(
    var cod: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fchpub: Long = 0L,
    var precio: Double = 0.0,
    var stock: Int = 0,
    var autor: String = "",
    var portadaUri: String = "",
    var portadaNombre: String = ""

) {
    fun toMap(): Map<String, Any> = mapOf(
        "titulo" to titulo,
        "descripcion" to descripcion,
        "fchpub" to fchpub,
        "precio" to precio,
        "stock" to stock,
        "autor" to autor,
        "portadaUri" to portadaUri,
        "portadaNombre"  to portadaNombre
    )
}