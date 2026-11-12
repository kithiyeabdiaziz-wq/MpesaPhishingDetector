package com.example.abdiazizmpesaphishing

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAuthHelper(private val context: Context) {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 1. Register User
    fun registerUser(name: String, email: String, password: String, onResult: (Boolean, String) -> Unit) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User created in Auth. Now save their name to Firestore.
                    val userId = mAuth.currentUser?.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                onResult(true, "Account created successfully!")
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Auth created but failed to save profile: ${e.message}")
                            }
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    // 2. Login User
    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful!")
                } else {
                    onResult(false, task.exception?.message ?: "Login failed")
                }
            }
    }

    // 3. Logout User
    fun logoutUser() {
        mAuth.signOut()
    }

    // 4. Forgot Password
    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Password reset email sent!")
                } else {
                    onResult(false, task.exception?.message ?: "Failed to send reset email")
                }
            }
    }
    // 5. Save Analysis History to Firestore
    fun saveAnalysisToHistory(
        type: String, // "SMS" or "URL"
        input: String,
        classification: String,
        riskScore: Int,
        explanation: String,
        recommendation: String
    ) {
        val userId = mAuth.currentUser?.uid ?: return
        val historyRef = db.collection("users").document(userId).collection("history").document()

        val record = hashMapOf(
            "type" to type,
            "input" to input,
            "classification" to classification,
            "riskScore" to riskScore,
            "explanation" to explanation,
            "recommendation" to recommendation,
            "timestamp" to System.currentTimeMillis()
        )

        historyRef.set(record)
    }
}