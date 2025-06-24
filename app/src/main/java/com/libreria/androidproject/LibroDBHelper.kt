package com.libreria.androidproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LibroDBHelper(context: Context) :
    SQLiteOpenHelper(context, "libreria.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE libro (
              cod INTEGER PRIMARY KEY AUTOINCREMENT,
              titulo TEXT,
              descripcion TEXT,
              fchpub INTEGER,
              precio REAL,
              stock INTEGER,
              autor TEXT,
              portadaUri TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS libro")
        onCreate(db)
    }

    fun insertarLibro(libro: Libro): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", libro.titulo)
            put("descripcion", libro.descripcion)
            put("fchpub", libro.fchpub)
            put("precio", libro.precio)
            put("stock", libro.stock)
            put("autor", libro.autor)
            put("portadaUri", libro.portadaUri)
        }
        return db.insert("libro", null, values)
    }

    fun obtenerLibro(): List<Libro> {
        val lista = mutableListOf<Libro>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM libro", null)
        while (cursor.moveToNext()) {
            lista += Libro(
                cod = cursor.getInt(0),
                titulo = cursor.getString(1),
                descripcion = cursor.getString(2),
                fchpub = cursor.getLong(3),
                precio = cursor.getDouble(4),
                stock = cursor.getInt(5),
                autor = cursor.getString(6),
                portadaUri = cursor.getString(7) ?: ""
            )
        }
        cursor.close()
        return lista
    }

    fun actualizarLibro(libro: Libro): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", libro.titulo)
            put("descripcion", libro.descripcion)
            put("fchpub", libro.fchpub)
            put("precio", libro.precio)
            put("stock", libro.stock)
            put("autor", libro.autor)
            put("portadaUri", libro.portadaUri)
        }
        return db.update("libro", values, "cod=?", arrayOf(libro.cod.toString()))
    }

    fun eliminarLibro(cod: Int): Int {
        val db = writableDatabase
        return db.delete("libro", "cod=?", arrayOf(cod.toString()))
    }
}