package com.libreria.androidproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LibroDBHelper(context: Context) : SQLiteOpenHelper(context, "productos.db", null, 1){
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE libro (
            cod INTEGER PRIMARY KEY AUTOINCREMENT,
            titulo TEXT,
            precio REAL,
            stock INTEGER
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS libro")
        onCreate(db)
    }

    fun insertarLibro(libro: Libro): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", libro.titulo)
            put("precio", libro.precio)
            put("stock", libro.stock)
        }
        return db.insert("libro", null, values)
    }
    fun obtenerLibro(): List<Libro> {
        val lista = mutableListOf<Libro>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM libro", null)
        while (cursor.moveToNext()) {
            val libro = Libro(
                cod = cursor.getInt(0),
                titulo = cursor.getString(1),
                precio = cursor.getDouble(2),
                stock = cursor.getInt(3)
            )
            lista.add(libro)
        }
        cursor.close()
        return lista
    }
    fun actualizarLibro(libro: Libro): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", libro.titulo)
            put("precio", libro.precio)
            put("stock", libro.stock)
        }
        return db.update("libro", values, "cod=?", arrayOf(libro.cod.toString()))
    }
    fun eliminarLibro(cod: Int): Int {
        val db = writableDatabase
        return db.delete("libro", "cod=?", arrayOf(cod.toString()))
    }

}