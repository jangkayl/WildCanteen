package cit.edu.wildcanteen.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.FoodRepository
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set

        private const val PREF_NAME = "UserSession"
        private val prefs: SharedPreferences by lazy {
            appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        // GLOBAL USER DETAILS
        var id: String? = null
        var name: String? = null
        var profilePic: Int? = null
        var isLoggedIn: Boolean = false
        var password: String? = null
        var balance: Double? = null
        var orders: MutableList<Order> = mutableListOf()


        // LOAD USER DATA WHEN SESSION STARTS
        fun loadUserSession() {
            val defaultPic = R.drawable.hd_user
            id = prefs.getString("USERNAME", "17-2068-369")
            name = prefs.getString("NAME", "John Doe")
            profilePic = prefs.getInt("PROFILE_PIC", defaultPic)
            password = prefs.getString("PASSWORD", null)
            isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)

            printUserDetails()
        }

        // SAVE USER DATA
        fun saveUserDetails(id: String, name: String, profilePic: Int, password: String) {
            this.id = id
            this.name = name;
            this.profilePic = profilePic
            this.password = password
            isLoggedIn = true

            prefs.edit().apply {
                putString("ID", id)
                putString("NAME", name)
                putInt("PROFILE_PIC", profilePic)
                putString("PASSWORD", password)
                putBoolean("IS_LOGGED_IN", true)
                apply()
            }

            printUserDetails()
        }

        // CLEAR SESSION WHEN LOGOUT
        fun clearUserSession() {
            isLoggedIn = false
            prefs.edit().apply {
                putBoolean("IS_LOGGED_IN", false)
                apply()
            }
            printUserDetails()
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
            orders.add(order)
            saveOrders()
        }

        fun clearOrders() {
            orders.clear()
            saveOrders()
        }

        private fun printUserDetails() {
            println("ID: $id")
            println("Name: $name")
            println("ProfilePic: $profilePic")
            println("Password: $password")
            println("Is Logged In: $isLoggedIn")
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        loadUserSession()
        loadOrders()
        printUserDetails()
    }
}
