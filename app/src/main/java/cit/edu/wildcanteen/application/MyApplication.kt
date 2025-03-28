package cit.edu.wildcanteen.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import cit.edu.wildcanteen.FirebaseRepository
import cit.edu.wildcanteen.FoodRepository
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
        var profilePic: Int? = null
        var password: String? = null
        var userType: String? = null
        var balance: Double? = null
        var orders: MutableList<Order> = mutableListOf()

        // Load user session when app starts
        fun loadUserSession(context: Context) {
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val storedUserId = sharedPreferences.getString("ID", null)

            if (storedUserId != null) {
                FirebaseRepository().getUser(storedUserId, { user ->
                    if (user != null) {
                        loadUserDetails(user)

                        // Redirect to HomePage immediately
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

            printUserDetails()
        }

        // Clear user session
        fun clearUserSession() {
            currentUser = null
            isLoggedIn = false
            studentId = null
            name = null
            profilePic = null
            password = null

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
        fun saveOrders() {
            val json = Gson().toJson(orders)
            prefs.edit().putString("ORDERS", json).apply()
        }

        private fun loadOrders() {
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

            if (orders.isEmpty()) {
                orders = FoodRepository.getOrderLists().toMutableList()
                saveOrders()
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

            saveOrders()
            loadOrders()
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
        printUserDetails()
    }
}
