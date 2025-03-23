package cit.edu.wildcanteen

data class FoodItem(
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
    val totalAmount: Double,
    val timestamp: Long
)