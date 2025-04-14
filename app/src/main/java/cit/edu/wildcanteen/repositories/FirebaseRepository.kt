package cit.edu.wildcanteen.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.security.MessageDigest
import android.util.Base64
import android.util.Log
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.User
import com.google.firebase.firestore.ListenerRegistration

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val ordersCollection = db.collection("orders")
    private val foodCollection = db.collection("food_items")
    private val orderBatchesCollection = db.collection("order_batches")

    fun addFoodItem(foodItem: FoodItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val itemDocRef = foodCollection.document()

        itemDocRef.set(foodItem, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirebaseFood", "Food item added: ${foodItem.name}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFood", "Failed to add food item", e)
                onFailure(e)
            }
    }

    fun getFoodItems(onSuccess: (List<FoodItem>) -> Unit, onFailure: (Exception) -> Unit) {
        val foodItemsList = mutableListOf<FoodItem>()

        foodCollection.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirebaseFood", "No food items found in food_items.")
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                documents.mapNotNull { doc ->
                    val foodItem = FoodItem(
                        category = doc.getString("category") ?: "",
                        name = doc.getString("name") ?: "",
                        foodId = doc.getLong("foodId")?.toInt() ?: 0,
                        price = doc.getDouble("price") ?: 0.0,
                        rating = doc.getDouble("rating") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        isPopular = doc.getBoolean("popular") ?: false,
                    )

                    Log.d("FirebaseFood", "Fetched: $foodItem")
                    foodItem
                }.let {
                    foodItemsList.addAll(it)
                    Log.d("FirebaseFood", "Total fetched food items: ${foodItemsList.size}")
                    onSuccess(foodItemsList)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFood", "Error fetching food items", e)
                onFailure(e)
            }
    }

    fun getAllOrders(onSuccess: (List<Order>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("FirebaseOrders", "Fetching all orders")

        ordersCollection.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirebaseOrders", "No orders found")
                } else {
                    Log.d("FirebaseOrders", "Orders retrieved: ${documents.size()}")
                }

                val orders = documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        val itemsMap = data["items"] as? Map<*, *> ?: return@mapNotNull null

                        val foodItem = FoodItem(
                            category = itemsMap["category"] as? String ?: "",
                            name = itemsMap["name"] as? String ?: "",
                            foodId = itemsMap["foodId"] as? Int ?: 0,
                            price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                            rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            items = foodItem,
                            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseOrders", "Error parsing order: ${doc.id}", e)
                        null
                    }
                }

                onSuccess(orders)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseOrders", "Failed to fetch orders", exception)
                onFailure(exception)
            }
    }

    fun getOrders(userId: String, onSuccess: (List<Order>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("FirebaseOrders", "Fetching orders for user: $userId")

        ordersCollection.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirebaseOrders", "No orders found for user: $userId")
                } else {
                    Log.d("FirebaseOrders", "Orders retrieved: ${documents.size()}")
                }

                val orders = documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        val itemsMap = data["items"] as? Map<*, *> ?: return@mapNotNull null

                        val foodItem = FoodItem(
                            category = itemsMap["category"] as? String ?: "",
                            name = itemsMap["name"] as? String ?: "",
                            foodId = itemsMap["foodId"] as? Int ?: 0,
                            price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                            rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            items = foodItem,
                            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseOrders", "Error parsing order: ${doc.id}", e)
                        null
                    }
                }

                onSuccess(orders)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseOrders", "Failed to fetch orders", exception)
                onFailure(exception)
            }
    }

    fun saveOrders(orders: List<Order>, ordersToRemove: List<Order> = emptyList(), onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val batch = db.batch()

        for (order in ordersToRemove) {
            val orderRef = ordersCollection.document(order.orderId)
            batch.delete(orderRef)
        }

        for (order in orders) {
            val orderRef = ordersCollection.document(order.orderId)
            batch.set(orderRef, order, SetOptions.merge())
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("FirebaseSaveOrders", "Orders updated and removed successfully.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSaveOrders", "Failed to save and remove orders", e)
                onFailure(e)
            }
    }

    fun listenForFoodItemsUpdates(
        onUpdate: (List<FoodItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        return foodCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseFood", "Listen failed", error)
                onFailure(error)
                return@addSnapshotListener
            }
            val foodItems = snapshot?.documents?.mapNotNull { doc ->
                try {
                    FoodItem(
                        category = doc.getString("category") ?: "",
                        name = doc.getString("name") ?: "",
                        foodId = doc.getLong("foodId")?.toInt() ?: 0,
                        price = doc.getDouble("price") ?: 0.0,
                        rating = doc.getDouble("rating") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        isPopular = doc.getBoolean("popular") ?: false,
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseFood", "Error parsing food item", e)
                    null
                }
            } ?: emptyList()

            Log.d("FirebaseFood", "Food items changed: ${foodItems.size} items")
            onUpdate(foodItems)
        }
    }

    fun listenForOrderUpdates(userId: String, onUpdate: (List<Order>) -> Unit): ListenerRegistration {
        return ordersCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseOrders", "Listen failed", error)
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val itemsMap = data["items"] as? Map<*, *> ?: return@mapNotNull null

                        val foodItem = FoodItem(
                            category = itemsMap["category"] as? String ?: "",
                            name = itemsMap["name"] as? String ?: "",
                            foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                            price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                            rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            items = foodItem,
                            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseOrders", "Error parsing order", e)
                        null
                    }
                } ?: emptyList()

                onUpdate(orders)
            }
    }

    fun listenForAllOrderUpdates(onUpdate: (List<Order>) -> Unit): ListenerRegistration {
        return ordersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseOrders", "Listen failed", error)
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val itemsMap = data["items"] as? Map<*, *> ?: return@mapNotNull null

                        val foodItem = FoodItem(
                            category = itemsMap["category"] as? String ?: "",
                            name = itemsMap["name"] as? String ?: "",
                            foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                            price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                            rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            items = foodItem,
                            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseOrders", "Error parsing order", e)
                        null
                    }
                } ?: emptyList()

                onUpdate(orders)
            }
    }

    fun addOrderBatch(
        orderBatch: OrderBatch,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val batchDocRef = orderBatchesCollection.document(orderBatch.batchId)

        batchDocRef.set(orderBatch)
            .addOnSuccessListener {
                Log.d("FirebaseOrderBatch", "Order batch added: ${orderBatch.batchId}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOrderBatch", "Failed to add order batch", e)
                onFailure(e)
            }
    }

    fun getOrderBatches(
        userId: String? = null,
        onSuccess: (List<OrderBatch>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val query = if (userId != null) {
            orderBatchesCollection.whereEqualTo("userId", userId)
        } else {
            orderBatchesCollection
        }

        query.get()
            .addOnSuccessListener { documents ->
                val orderBatches = documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        val ordersList = (data["orders"] as? List<Map<String, Any>>)?.mapNotNull { orderData ->
                            try {
                                val itemsMap = orderData["items"] as? Map<String, Any> ?: return@mapNotNull null

                                Order(
                                    orderId = orderData["orderId"] as? String ?: "",
                                    userId = orderData["userId"] as? String ?: "",
                                    userName = orderData["userName"] as? String ?: "",
                                    items = FoodItem(
                                        category = itemsMap["category"] as? String ?: "",
                                        name = itemsMap["name"] as? String ?: "",
                                        foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                                        price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                                        rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                                        description = itemsMap["description"] as? String ?: "",
                                        imageUrl = itemsMap["imageUrl"] as? String ?: "",
                                        isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                                    ),
                                    quantity = (orderData["quantity"] as? Number)?.toInt() ?: 0,
                                    totalAmount = (orderData["totalAmount"] as? Number)?.toDouble() ?: 0.0
                                )
                            } catch (e: Exception) {
                                Log.e("FirebaseOrderBatch", "Error parsing order", e)
                                null
                            }
                        } ?: emptyList()

                        OrderBatch(
                            batchId = data["batchId"] as? String ?: doc.id,
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            orders = ordersList,
                            totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                            status = data["status"] as? String ?: "Pending",
                            paymentMethod = data["paymentMethod"] as? String ?: "",
                            deliveryType = data["deliveryType"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseOrderBatch", "Error parsing order batch", e)
                        null
                    }
                }.sortedByDescending { it.timestamp }

                onSuccess(orderBatches)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseOrderBatch", "Failed to fetch order batches", exception)
                onFailure(exception)
            }
    }

    fun listenForOrderBatches(
        userId: String? = null,
        onUpdate: (List<OrderBatch>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        val query = if (userId != null) {
            orderBatchesCollection.whereEqualTo("userId", userId)
        } else {
            orderBatchesCollection
        }

        return query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onFailure(error)
                return@addSnapshotListener
            }

            val orderBatches = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val ordersList = (data["orders"] as? List<Map<String, Any>>)?.mapNotNull { orderData ->
                        try {
                            val itemsMap = orderData["items"] as? Map<String, Any> ?: return@mapNotNull null

                            Order(
                                orderId = orderData["orderId"] as? String ?: "",
                                userId = orderData["userId"] as? String ?: "",
                                userName = orderData["userName"] as? String ?: "",
                                items = FoodItem(
                                    category = itemsMap["category"] as? String ?: "",
                                    name = itemsMap["name"] as? String ?: "",
                                    foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                                    price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                                    rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                                    description = itemsMap["description"] as? String ?: "",
                                    imageUrl = itemsMap["imageUrl"] as? String ?: "",
                                    isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                                ),
                                quantity = (orderData["quantity"] as? Number)?.toInt() ?: 0,
                                totalAmount = (orderData["totalAmount"] as? Number)?.toDouble() ?: 0.0
                            )
                        } catch (e: Exception) {
                            Log.e("FirebaseOrderBatch", "Error parsing order", e)
                            null
                        }
                    } ?: emptyList()

                    OrderBatch(
                        batchId = data["batchId"] as? String ?: doc.id,
                        userId = data["userId"] as? String ?: "",
                        userName = data["userName"] as? String ?: "",
                        orders = ordersList,
                        totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                        status = data["status"] as? String ?: "Pending",
                        paymentMethod = data["paymentMethod"] as? String ?: "",
                        deliveryType = data["deliveryType"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseOrderBatch", "Error parsing order batch", e)
                    null
                }
            }?.sortedByDescending { it.timestamp } ?: emptyList()

            onUpdate(orderBatches)
        }
    }

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val hashedPassword = hashPassword(user.password)

        val userWithHashedPassword = User(
            studentId = user.studentId,
            profileImageUrl = user.profileImageUrl,
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
                            profileImageUrl = data["profileImageUrl"] as? String ?: "Unkown",
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

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
