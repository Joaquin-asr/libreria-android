package com.libreria.androidproject.auth


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(email: String, pass: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        return result.user
    }

    fun signOut() {
        auth.signOut()
    }

    fun currentUser(): FirebaseUser? = auth.currentUser
}