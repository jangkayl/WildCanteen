package cit.edu.wildcanteen.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.security.MessageDigest
import android.util.Base64
import android.util.Log
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.User
import cit.edu.wildcanteen.UserInfo
import cit.edu.wildcanteen.application.MyApplication
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class FirebaseRepository {
    val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val ordersCollection = db.collection("orders")
    private val foodCollection = db.collection("food_items")
    private val orderBatchesCollection = db.collection("order_batches")
    private val chatMessagesCollection = db.collection("chat_messages")
    private val feedbacksCollection = db.collection("feedbacks")

    fun addFeedback(feedback: Feedback, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val itemDocRef = feedbacksCollection.document()

        itemDocRef.set(feedback, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirebaseFeedback", "Feedback item added: ${feedback.name}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFeedback", "Failed to add feedback", e)
                onFailure(e)
            }
    }

    fun getFeedbacksForFoodListener(
        foodId: Int,
        onFeedbackReceived: (List<Feedback>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return feedbacksCollection
            .whereEqualTo("foodId", foodId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseFeedback", "Error fetching feedbacks: ${error.message}", error)
                    onError(error)
                    return@addSnapshotListener
                }

                Log.d("FirebaseDebug", "Got ${snapshot?.size()} documents")
                val feedbacks = mutableListOf<Feedback>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val feedback = Feedback(
                            foodId = doc.getLong("foodId")?.toInt() ?: 0,
                            userId = doc.getString("userId") ?: "",
                            name = doc.getString("name") ?: "",
                            profileImageUrl = doc.getString("profileImageUrl") ?: "",
                            rating = doc.getDouble("rating") ?: 0.0,
                            imageUrl = doc.get("imageUrl") as? List<String> ?: emptyList(),
                            feedback = doc.getString("feedback") ?: "",
                            likes = doc.getLong("likes")?.toInt() ?: 0,
                            disLikes = doc.getLong("disLikes")?.toInt() ?: 0,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                        feedbacks.add(feedback)
                    } catch (e: Exception) {
                        Log.e("FirebaseFeedback", "Error parsing feedback document ${doc.id}", e)
                    }
                }

                onFeedbackReceived(feedbacks)
            }
    }

    fun sendChatMessage(
        chatMessage: ChatMessage,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val messageDocRef = chatMessagesCollection.document()

        val messageWithId = chatMessage.copy(messageId = messageDocRef.id)

        messageDocRef.set(messageWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun markMessageAsRead(
        messageId: String,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = { Log.e("FirebaseRepo", "Error marking message as read", it) }
    ) {
        chatMessagesCollection.document(messageId)
            .update("isRead", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun listenForUserChats(
        userId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        val query = chatMessagesCollection
            .where(
                Filter.or(
                Filter.equalTo("senderId", userId),
                Filter.equalTo("recipientId", userId)
            ))
            .orderBy("timestamp", Query.Direction.DESCENDING)

        return query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onFailure(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                try {
                    ChatMessage(
                        messageId = doc.id,
                        roomId = doc.getString("roomId") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        senderImage = doc.getString("senderImage") ?: "",
                        recipientId = doc.getString("recipientId") ?: "",
                        recipientName = doc.getString("recipientName") ?: "",
                        recipientImage = doc.getString("recipientImage") ?: "",
                        messageText = doc.getString("messageText") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseChat", "Error parsing message", e)
                    null
                }
            } ?: emptyList()

            val latestMessages = messages
                .groupBy { it.roomId }
                .mapNotNull { (_, messages) -> messages.maxByOrNull { it.timestamp } }
                .sortedByDescending { it.timestamp }

            onUpdate(latestMessages)
        }
    }

    fun listenForConversationMessages(
        userId1: String,
        userId2: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        val roomId = listOf(userId1, userId2).sorted().joinToString("_")

        return chatMessagesCollection
            .whereEqualTo("roomId", roomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            messageId = doc.id,
                            roomId = doc.getString("roomId") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            senderImage = doc.getString("senderImage") ?: "",
                            recipientId = doc.getString("recipientId") ?: "",
                            recipientName = doc.getString("recipientName") ?: "",
                            recipientImage = doc.getString("recipientImage") ?: "",
                            messageText = doc.getString("messageText") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseChat", "Error parsing message", e)
                        null
                    }
                } ?: emptyList()

                onUpdate(messages)
            }
    }

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
                        canteenId = doc.getString("canteenId") ?: "",
                        canteenName = doc.getString("canteenName") ?: "",
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
                            canteenId = itemsMap["canteenId"] as? String?: "",
                            canteenName = itemsMap["canteenName"] as? String ?: "",
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            canteenId = data["canteenId"] as? String?: "",
                            canteenName = data["canteenName"] as? String ?: "",
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
                            canteenId = itemsMap["canteenId"] as? String?: "",
                            canteenName = itemsMap["canteenName"] as? String ?: "",
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            canteenId = data["canteenId"] as? String?: "",
                            canteenName = data["canteenName"] as? String ?: "",
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
            Log.e("FirebaseFoodItem", "Listening Food Item Updates", error)
            val foodItems = snapshot?.documents?.mapNotNull { doc ->
                try {
                    FoodItem(
                        category = doc.getString("category") ?: "",
                        name = doc.getString("name") ?: "",
                        foodId = doc.getLong("foodId")?.toInt() ?: 0,
                        price = doc.getDouble("price") ?: 0.0,
                        rating = doc.getDouble("rating") ?: 0.0,
                        canteenId = doc.getString("canteenId") ?: "",
                        canteenName = doc.getString("canteenName") ?: "",
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

                Log.e("FirebaseOrders", "Listening Order Updates", error)
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
                            canteenId = itemsMap["canteenId"] as? String?: "",
                            canteenName = itemsMap["canteenName"] as? String ?: "",
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            canteenId = data["canteenId"] as? String?: "",
                            canteenName = data["canteenName"] as? String ?: "",
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

                Log.e("FirebaseOrders", "Listening All Order Updates", error)
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
                            canteenId = itemsMap["canteenId"] as? String?: "",
                            canteenName = itemsMap["canteenName"] as? String ?: "",
                            description = itemsMap["description"] as? String ?: "",
                            imageUrl = itemsMap["imageUrl"] as? String ?: "",
                            isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                        )

                        Order(
                            orderId = data["orderId"] as? String ?: "",
                            canteenId = data["canteenId"] as? String?: "",
                            canteenName = data["canteenName"] as? String ?: "",
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

    fun getAllOrdersFromBatch(batchId: String, onSuccess: (List<Order>) -> Unit, onFailure: (Exception) -> Unit) {
        orderBatchesCollection.document(batchId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val data = document.data ?: throw Exception("Document data is null")

                        val ordersList = (data["orders"] as? List<Map<String, Any>>)?.mapNotNull { orderData ->
                            try {
                                val itemsMap = orderData["items"] as? Map<String, Any> ?: return@mapNotNull null

                                Order(
                                    orderId = orderData["orderId"] as? String ?: "",
                                    canteenId = orderData["canteenId"] as? String ?: "",
                                    canteenName = orderData["canteenName"] as? String ?: "",
                                    userId = orderData["userId"] as? String ?: "",
                                    userName = orderData["userName"] as? String ?: "",
                                    items = FoodItem(
                                        category = itemsMap["category"] as? String ?: "",
                                        name = itemsMap["name"] as? String ?: "",
                                        foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                                        price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                                        rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                                        canteenId = itemsMap["canteenId"] as? String ?: "",
                                        canteenName = itemsMap["canteenName"] as? String ?: "",
                                        description = itemsMap["description"] as? String ?: "",
                                        imageUrl = itemsMap["imageUrl"] as? String ?: "",
                                        isPopular = itemsMap["isPopular"] as? Boolean ?: false,
                                    ),
                                    quantity = (orderData["quantity"] as? Number)?.toInt() ?: 0,
                                    totalAmount = (orderData["totalAmount"] as? Number)?.toDouble() ?: 0.0
                                )
                            } catch (e: Exception) {
                                Log.e("FirebaseOrder", "Error parsing order", e)
                                null
                            }
                        } ?: emptyList()

                        onSuccess(ordersList)
                    } catch (e: Exception) {
                        Log.e("FirebaseOrderBatch", "Error parsing order batch document", e)
                        onFailure(e)
                    }
                } else {
                    onFailure(Exception("Order batch not found"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseOrderBatch", "Failed to fetch order batch", exception)
                onFailure(exception)
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

        Log.e("Firebase", "Getting Order Batches")
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
                                    canteenId = orderData["canteenId"] as? String?: "",
                                    canteenName = orderData["canteenName"] as? String ?: "",
                                    userId = orderData["userId"] as? String ?: "",
                                    userName = orderData["userName"] as? String ?: "",
                                    items = FoodItem(
                                        category = itemsMap["category"] as? String ?: "",
                                        name = itemsMap["name"] as? String ?: "",
                                        foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                                        price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                                        rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                                        canteenId = itemsMap["canteenId"] as? String?: "",
                                        canteenName = itemsMap["canteenName"] as? String ?: "",
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
                            referenceNumber = data["referenceNumber"] as? String ?: "",
                            deliveryType = data["deliveryType"] as? String ?: "",
                            deliveredBy = data["deliveredBy"] as? String ?: "",
                            deliveredByName = data["deliveredByName"] as? String ?: "",
                            deliveryAddress = data["deliveryAddress"] as? String ?: "",
                            deliveryFee = data["deliveryFee"] as? Double ?: 0.0,
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

        Log.e("Firebase", "Listening Order Batches")
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
                                canteenId = orderData["canteenId"] as? String ?: "",
                                canteenName = orderData["canteenName"] as? String ?: "",
                                userId = orderData["userId"] as? String ?: "",
                                userName = orderData["userName"] as? String ?: "",
                                items = FoodItem(
                                    category = itemsMap["category"] as? String ?: "",
                                    name = itemsMap["name"] as? String ?: "",
                                    foodId = (itemsMap["foodId"] as? Number)?.toInt() ?: 0,
                                    price = (itemsMap["price"] as? Number)?.toDouble() ?: 0.0,
                                    rating = (itemsMap["rating"] as? Number)?.toDouble() ?: 0.0,
                                    canteenName = itemsMap["canteenName"] as? String ?: "",
                                    canteenId = itemsMap["canteenId"] as? String ?: "",
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
                        referenceNumber = data["referenceNumber"] as? String ?: "",
                        deliveryType = data["deliveryType"] as? String ?: "",
                        deliveredBy = data["deliveredBy"] as? String ?: "",
                        deliveredByName = data["deliveredByName"] as? String ?: "",
                        deliveryAddress = data["deliveryAddress"] as? String ?: "",
                        deliveryFee = data["deliveryFee"] as? Double ?: 0.0,
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

    fun acceptDeliveryOrder(batchId: String, studentId: String, name: String, callback: (Boolean, Exception?) -> Unit) {
        val batchRef = orderBatchesCollection.document(batchId)

        batchRef.update("deliveredBy", studentId, "status", "Delivering", "deliveredByName", name)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e) }
    }

    fun markOrderBatchAsCompleted(batchId: String, callback: (Boolean, Exception?) -> Unit) {
        val batchRef = orderBatchesCollection.document(batchId)

        batchRef.update("status", "Completed")
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e) }
    }

    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val hashedPassword = hashPassword(user.password)

        val userWithHashedPassword = User(
            studentId = user.studentId,
            profileImageUrl = user.profileImageUrl,
            name = user.name,
            password = hashedPassword,
            userType = user.userType,
        )

        usersCollection.document(user.studentId)
            .set(userWithHashedPassword, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getAllUsers(
        onSuccess: (List<UserInfo>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {
        return usersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    Log.d("FirebaseRepo", "Document data: ${doc.data}")
                    try {
                        UserInfo(
                            userId = doc.id,
                            name = doc.getString("name"),
                            profileImageUrl = doc.getString("profileImageUrl")
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseRepo", "Error parsing user", e)
                        null
                    }
                } ?: emptyList()

                onSuccess(users)
            }
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
                        )
                    }
                    onSuccess(user)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserProfileImageUrl(
        userId: String,
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit = { Log.e("FirebaseRepo", "Error fetching user info", it) }
    ) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    onSuccess(userInfo?.profileImageUrl)
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
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
