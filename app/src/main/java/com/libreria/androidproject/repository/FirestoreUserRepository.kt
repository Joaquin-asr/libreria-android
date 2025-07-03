package com.libreria.androidproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.libreria.androidproject.model.User

class FirestoreUserRepository {
    private val db = FirebaseFirestore.getInstance().collection("users")
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUser(uid: String): User? {
        val snapshot = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun addUser(uid: String, email: String, role: String = "Usuario") {
        val data = mapOf(
            "email" to email,
            "role"  to role
        )
        db.document(uid).set(data).await()
    }
}