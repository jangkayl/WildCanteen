package cit.edu.wildcanteen.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.repositories.FirebaseRepository
import cit.edu.wildcanteen.pages.student_pages.HomePageActivity
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.User
import cit.edu.wildcanteen.pages.admin_page.AdminTemporaryActivity
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    companion object {
        var isLoggedIn: Boolean = false
        private var currentUser: User? = null
        private lateinit var firebaseRepository: FirebaseRepository

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
        var orders: MutableList<Order> = mutableListOf()
        var foodItems: MutableList<FoodItem> = mutableListOf()
        var popularFoodItems: MutableList<FoodItem> = mutableListOf()

        // Global chat-related data
        var allChats: MutableList<ChatMessage> = mutableListOf()
        var chatListener: ListenerRegistration? = null
        val chatUpdatesLiveData = MutableLiveData<List<ChatMessage>>()

        fun loadUserSession(context: Context) {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val storedUserId = sharedPreferences.getString("ID", null)
            loadFoodItems()

            if (storedUserId != null) {
                FirebaseRepository().getUser(storedUserId, { user ->
                    if (user != null) {
                        loadUserDetails(user)
                    }
                }, {
                    clearUserSession()
                })
            }
            printUserDetails()
        }

        private fun initializeChat() {
            firebaseRepository = FirebaseRepository()
            val currentUserId = studentId ?: return
            Log.d("MyApplication", "Chats messages listening starting")

            clearChatListener()

            chatListener = firebaseRepository.listenForUserChats(
                currentUserId,
                onUpdate = { messages ->
                    Log.d("MyApplication", "Received ${messages.size} chat messages from Firebase")

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        val updatedChats = messages.toMutableList()

                        allChats = updatedChats

                        chatUpdatesLiveData.value = updatedChats
                        Log.d("MyApplication", "LiveData updated with ${updatedChats.size} messages")
                    }
                },
                onFailure = { exception ->
                    Log.e("MyApplication", "Error listening for chats", exception)
                }
            )
        }

        private fun clearChatListener() {
            allChats.clear()
            chatListener?.remove()
            chatListener = null
            Log.d("MyApplication", "Chat listener removed")
        }

        // Load user details
        fun loadUserDetails(user: User) {
            studentId = user.studentId
            stringStudentId = getFormattedStudentId()
            name = user.name
            profileImageUrl = user.profileImageUrl
            password = user.password
            userType = user.userType
            isLoggedIn = true
            currentUser = user
            loadOrders()

            prefs.edit().apply {
                putString("ID", user.studentId)
                putString("NAME", user.name)
                putString("PASSWORD", user.password)
                putBoolean("IS_LOGGED_IN", true)
                apply()
            }

            initializeChat()
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
            chatUpdatesLiveData.postValue(emptyList())
            clearChatListener()

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
            Log.d("OrderDebug", "Saving orders: ${orders.joinToString { "${it.orderId}" }}")

            FirebaseRepository().saveOrders(orders, ordersToRemove, {
                Log.d("OrderDebug", "Orders saved successfully to Firebase")
            }, { e ->
                Log.e("OrderDebug", "Error saving orders: ${e.message}")
            })
        }

        private fun loadOrders() {
            if (studentId.isNullOrEmpty()) {
                Log.e("OrderDebug", "Cannot load orders: studentId is null or empty")
                return
            }

            FirebaseRepository().getOrders(studentId!!, { ordersFromFirebase ->
                Log.d("OrderDebug", "Loaded from Firebase: ${ordersFromFirebase.size} orders")
                orders = ordersFromFirebase.toMutableList()

                val json = prefs.getString("ORDERS", null)
                if (!json.isNullOrEmpty()) {
                    try {
                        val type = object : TypeToken<MutableList<Order>>() {}.type
                        val cachedOrders = Gson().fromJson<MutableList<Order>>(json, type) ?: mutableListOf()
                        Log.d("OrderDebug", "Loaded from cache: ${cachedOrders.size} orders")

                        // Merge Firebase and cached orders, preferring Firebase data
                        orders.addAll(cachedOrders.filter { cached ->
                            orders.none { it.orderId == cached.orderId }
                        })
                    } catch (e: Exception) {
                        Log.e("OrderDebug", "Error loading cached orders", e)
                    }
                }

                Log.d("OrderDebug", "Final orders: ${orders.size} items")
                orders.forEach { Log.d("OrderDebug", "Order ${it.orderId}") }
            }, { exception ->
                Log.e("OrderDebug", "Failed to load orders from Firebase", exception)

                val json = prefs.getString("ORDERS", null)
                if (!json.isNullOrEmpty()) {
                    try {
                        val type = object : TypeToken<MutableList<Order>>() {}.type
                        orders = Gson().fromJson(json, type) ?: mutableListOf()
                        Log.d("OrderDebug", "Loaded from cache (fallback): ${orders.size} orders")
                    } catch (e: Exception) {
                        Log.e("OrderDebug", "Error loading cached orders (fallback)", e)
                        orders = mutableListOf()
                    }
                }
            })
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

        fun clearOrdersCache() {
            prefs.edit().remove("ORDERS").apply()
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
            CoroutineScope(Dispatchers.IO).launch {
                FirebaseRepository().getFoodItems({ foodList ->
                    CoroutineScope(Dispatchers.Main).launch {
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
                    }
                }, { e ->
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.e("FirebaseFood", "Failed to load food items from Firebase", e)
                        loadFromCache()
                        onComplete?.invoke()
                    }
                })
            }
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
        printUserDetails()
    }
}
