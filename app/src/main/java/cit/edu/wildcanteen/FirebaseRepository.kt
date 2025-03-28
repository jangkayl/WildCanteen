package cit.edu.wildcanteen

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.security.MessageDigest
import android.util.Base64


class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val hashedPassword = hashPassword(user.password)

        val userWithHashedPassword = User(
            studentId = user.studentId,
            name = user.name,
            password = hashedPassword,
            userType = user.userType,
            balance = user.balance
        )

        usersCollection.document(user.studentId)
            .set(userWithHashedPassword, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUser(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.data?.let { data ->
                        User(
                            studentId = userId,
                            name = data["name"] as? String ?: "Unknown",
                            password = data["password"] as? String ?: "",
                            userType = data["userType"] as? String ?: "",
                            balance = (data["balance"] as? Number)?.toDouble() ?: 0.0
                        )
                    }
                    onSuccess(user)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateUser(userId: String, updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (updates.isEmpty()) {
            onFailure(Exception("No changes to update"))
            return
        }

        val modifiedUpdates = updates.toMutableMap()

        val password = updates["password"] as? String
        if (!password.isNullOrBlank()) {
            modifiedUpdates["password"] = hashPassword(password)
        }

        usersCollection.document(userId)
            .set(modifiedUpdates, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection.document(userId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
