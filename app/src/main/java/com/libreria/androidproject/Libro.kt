package com.libreria.androidproject


data class Libro(
    var cod: Int = 0,
    var titulo: String,
    var descripcion: String,
    var fchpub: Long,
    var precio: Double,
    var stock: Int,
    var autor: String,
    var portadaUri: String = ""
)