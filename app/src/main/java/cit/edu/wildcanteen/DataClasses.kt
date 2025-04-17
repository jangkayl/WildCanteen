package cit.edu.wildcanteen

data class User(
    val studentId: String,
    val profileImageUrl: String,
    val name: String,
    val password: String,
    val userType: String,
)

data class FoodItem(
    val category: String,
    val foodId: Int,
    val name: String,
    val price: Double,
    val rating: Double,
    val canteenId: String,
    val canteenName: String,
    val description: String,
    val imageUrl: String,
    val isPopular: Boolean,
)

data class CategoryItem(
    val name: String,
    val imageRes: Int,
    val backgroundColor: Int
)

data class Order(
    val orderId: String,
    val canteenId: String,
    val canteenName: String,
    val userId: String,
    val userName: String,
    val items: FoodItem,
    var quantity: Int,
    var totalAmount: Double,
)

data class CanteenOrderGroup(
    val canteenName: String,
    val orders: List<Order>
)

data class OrderBatch(
    val batchId: String,
    val userId: String,
    val userName: String,
    val orders: List<Order>,
    val totalAmount: Double,
    val status: String,
    val paymentMethod: String,
    val referenceNumber: String,
    val deliveryType: String,
    val deliveredBy: String,
    val deliveryAddress: String,
    val deliveryFee: Double,
    val timestamp: Long
)

data class Feedback(
    val foodId: Int,
    val userId: String,
    val name: String,
    val profileImageUrl: String,
    val rating: Double,
    val imageUrl: List<String>,
    val text: String,
    val timestamp: Long
)

data class ChatMessage(
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val senderImage: String,
    val recipientId: String,
    val messageText: String,
    val timestamp: Long,
    val isRead: Boolean
)

data class ChatRoom(
    val roomId: String,
    val participantIds: List<String>,
    val lastMessage: ChatMessage,
    val timestamp: Long
)

data class Message(
    val id: String,
    val text: String,
    val senderId: String,
    val timestamp: Long,
    val isSent: Boolean,
    val isRead: Boolean
)

