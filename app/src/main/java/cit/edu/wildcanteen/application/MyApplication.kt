package cit.edu.wildcanteen.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.repositories.FirebaseRepository
import cit.edu.wildcanteen.HomePageActivity
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyApplication : Application() {
    companion object {
        var isLoggedIn: Boolean = false
        private var currentUser: User? = null

        lateinit var appContext: Context
            private set

        private const val PREF_NAME = "UserSession"
        private val prefs: SharedPreferences by lazy {
            appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        // Global user details
        var studentId: String? = null
        var stringStudentId: String? = null
        var name: String? = null
        var profileImageUrl: String? = null
        var password: String? = null
        var userType: String? = null
        var balance: Double? = null
        var orders: MutableList<Order> = mutableListOf()
        var foodItems: MutableList<FoodItem> = mutableListOf()
        var popularFoodItems: MutableList<FoodItem> = mutableListOf()

        fun loadUserSession(context: Context) {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val storedUserId = sharedPreferences.getString("ID", null)

            if (storedUserId != null) {
                FirebaseRepository().getUser(storedUserId, { user ->
                    if (user != null) {
                        loadUserDetails(user)
                        val intent = Intent(context, HomePageActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                }, {
                    clearUserSession()
                })
            }
            printUserDetails()
        }


        // Load user details
        fun loadUserDetails(user: User) {
            studentId = user.studentId
            stringStudentId = getFormattedStudentId()
            name = user.name
            profileImageUrl = user.profileImageUrl
            password = user.password
            userType = user.userType
            balance = user.balance
            isLoggedIn = true
            currentUser = user

            prefs.edit().apply {
                putString("ID", user.studentId)
                putString("NAME", user.name)
                putString("PASSWORD", user.password)
                putBoolean("IS_LOGGED_IN", true)
                apply()
            }
            loadOrders()
            printUserDetails()
        }

        // Clear user session
        fun clearUserSession() {
            currentUser = null
            isLoggedIn = false
            studentId = null
            name = null
            profileImageUrl = null
            password = null
            orders.clear()

            prefs.edit().clear().apply()
            printUserDetails()
        }

        fun getFormattedStudentId(): String? {
            val cleanedId = studentId?.filter { it.isDigit() }

            return if (cleanedId?.length == 9) {
                listOf(cleanedId.substring(0, 2), cleanedId.substring(2, 6), cleanedId.substring(6, 9))
                    .joinToString("-")
            } else {
                null
            }
        }

        /////     FOR ORDERS
        fun saveOrders(ordersToRemove: List<Order>) {
            val json = Gson().toJson(orders)
            prefs.edit().putString("ORDERS", json).apply()

            FirebaseRepository().saveOrders(orders, ordersToRemove, {
                Log.d("FirebaseOrders", "Orders saved successfully.")
            }, { e ->
                Log.e("FirebaseOrders", "Error saving orders: ${e.message}")
            })

        }

        private fun loadOrders() {
            if (studentId.isNullOrEmpty()) {
                Log.e("FirebaseOrders", "Cannot load orders: $studentId is null or empty")
                return
            }
            if (studentId != null) {
                FirebaseRepository().getOrders(studentId!!, { ordersFromFirebase ->
                    orders = ordersFromFirebase.toMutableList()
                    Log.d("FirebaseOrders", "All orders from $studentId are now loaded successfully")
                }, { exception ->
                    Log.e("FirebaseOrders", "Failed to load orders for $studentId", exception)
                    orders = mutableListOf()
                })
            }

            val json = prefs.getString("ORDERS", null)
            if (!json.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<MutableList<Order>>() {}.type
                    orders = Gson().fromJson(json, type) ?: mutableListOf()
                } catch (e: Exception) {
                    e.printStackTrace()
                    orders = mutableListOf()
                }
            }
        }

        fun addOrder(order: Order) {
            val existingOrder = orders.find { it.items.name == order.items.name }

            if (existingOrder != null) {
                existingOrder.quantity = order.quantity
                existingOrder.totalAmount = existingOrder.quantity * existingOrder.items.price
            } else {
                orders.add(order)
            }

            saveOrders(emptyList())
        }


        fun saveFoodItems(items: List<FoodItem>) {
            prefs.edit().remove("FOOD_ITEMS").apply()

            foodItems = items.toMutableList()

            val json = Gson().toJson(foodItems)
            prefs.edit().putString("FOOD_ITEMS", json).apply()

            popularFoodItems = foodItems.filter { it.isPopular }.toMutableList()

            Log.d("PopularFoodItems", "Updated Popular Food Items: ${popularFoodItems.size}")
        }


        fun loadFoodItems(onComplete: (() -> Unit)? = null) {
            FirebaseRepository().getFoodItems({ foodList ->
                if (foodList.isNotEmpty()) {
                    foodItems.apply {
                        clear()
                        addAll(foodList)
                    }

                    popularFoodItems.apply {
                        clear()
                        addAll(foodList.filter { it.isPopular })
                    }

                    saveFoodItems(foodList)

                    Log.d("FirebaseFood", "Food items loaded: ${foodItems.size}")
                    Log.d("PopularFoodItems", "Popular food items updated: ${popularFoodItems.size}")
                } else {
                    Log.d("FirebaseFood", "No food items found in Firebase. Loading cached food items.")
                    loadFromCache()
                }

                onComplete?.invoke()
            }, { e ->
                Log.e("FirebaseFood", "Failed to load food items from Firebase", e)
                loadFromCache()
                onComplete?.invoke()
            })
        }


        private fun loadFromCache() {
            val json = prefs.getString("FOOD_ITEMS", null)
            if (!json.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<MutableList<FoodItem>>() {}.type
                    foodItems = Gson().fromJson(json, type) ?: mutableListOf()
                    popularFoodItems = foodItems.filter { it.isPopular }.toMutableList()
                    Log.d("PopularFoodItems", "Loaded food items from cache: ${foodItems.size}")
                } catch (e: Exception) {
                    Log.e("PopularFoodItems", "Error loading cached food items", e)
                    foodItems = mutableListOf()
                    popularFoodItems = mutableListOf()
                }
            }
        }

        private fun printUserDetails() {
            Log.e("User details", currentUser.toString());
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        loadUserSession(this)
        loadOrders()
        loadFoodItems()
        printUserDetails()
    }
}
