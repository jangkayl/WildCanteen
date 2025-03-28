package cit.edu.wildcanteen

data class FoodItem(
    val category: String,
    val name: String,
    val price: Double,
    val rating: Double,
    val description: String,
    val imageResId: Int
)

data class CategoryItem(
    val name: String,
    val imageRes: Int,
    val backgroundColor: Int
)

data class Order(
    val orderId: String,
    val items: FoodItem,
    var quantity: Int,
    var totalAmount: Double,
    val timestamp: Long
)

data class User(
    val studentId: String,
    val name: String,
    val password: String,
    val userType: String,
    val balance: Double
)
