package cit.edu.wildcanteen

data class User(
    val studentId: String,
    val profileImageUrl: String,
    val name: String,
    val password: String,
    val userType: String,
    val balance: Double
)

data class FoodItem(
    val category: String,
    val foodId: Int,
    val name: String,
    val price: Double,
    val rating: Double,
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
    val userId: String,
    val userName: String,
    val items: FoodItem,
    var quantity: Int,
    var totalAmount: Double,
)

data class OrderBatch(
    val batchId: String,
    val userId: String,
    val userName: String,
    val orders: List<Order>,
    val totalAmount: Double,
    val status: String,
    val paymentMethod: String,
    val deliveryType: String,
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

