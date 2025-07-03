package com.libreria.androidproject.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.libreria.androidproject.model.Order
import kotlinx.coroutines.tasks.await

class FirestoreOrderRepository {
    private val db = FirebaseFirestore.getInstance().collection("orders")

    suspend fun addOrder(order: Order): String {
        val ref = db.add(order.toMap()).await()
        return ref.id
    }

    suspend fun getOrdersForUser(uid: String): List<Order> {
        val snap = db.whereEqualTo("userId", uid).get(Source.CACHE).await()
        return snap.documents.map {
            it.toObject(Order::class.java)!!.apply { id = it.id }
        }
    }
}