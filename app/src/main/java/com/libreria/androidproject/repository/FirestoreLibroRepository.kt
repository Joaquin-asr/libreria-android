package com.libreria.androidproject.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.libreria.androidproject.model.Libro
import kotlinx.coroutines.tasks.await

class FirestoreLibroRepository {
    private val db = FirebaseFirestore.getInstance().collection("libros")

    suspend fun addLibro(libro: Libro): String {
        val ref = db.add(libro.toMap()).await()
        return ref.id
    }

    suspend fun updateLibro(libro: Libro) {
        db.document(libro.cod).set(libro.toMap()).await()
    }

    suspend fun deleteLibro(cod: String) {
        db.document(cod).delete().await()
    }

    suspend fun getAllLibrosFromCache(): List<Libro> {
        val snap = db.get(Source.CACHE).await()
        return snap.documents.map {
            it.toObject(Libro::class.java)!!.apply { cod = it.id }
        }
    }

    fun listenAllLibros(onChange: (List<Libro>) -> Unit): ListenerRegistration {
        return db.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val lista = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Libro::class.java)?.apply { cod = doc.id }
            }
            onChange(lista)
        }
    }
}