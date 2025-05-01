package cit.edu.wildcanteen.pages.student_pages

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.CategoryItem
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CategoryAdapter
import cit.edu.wildcanteen.adapters.FoodAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.ceil

class HomePageOrderActivity : Activity() {
    private var foodItemsListener: ListenerRegistration? = null
    private lateinit var popularAdapter: FoodAdapter
    private lateinit var allMenuAdapter: FoodAdapter
    private lateinit var allFoodItems: List<FoodItem>
    private lateinit var popularFoodItems: List<FoodItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.homepage_main)

        findViewById<LinearLayout>(R.id.logoBack).setOnClickListener { finish() }

        setupPopularRecycler()
        setupCategoriesRecycler()
        setupAllMenuRecycler()
        setupFoodItemsListener() // Initialize Firebase listener
    }

    private fun setupPopularRecycler() {
        popularFoodItems = MyApplication.popularFoodItems.toList()
        popularAdapter = FoodAdapter(popularFoodItems.toMutableList(), this, R.layout.food_item_popular)

        findViewById<RecyclerView>(R.id.popularNowRecycleView).apply {
            layoutManager = LinearLayoutManager(this@HomePageOrderActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularAdapter
        }
    }

    private fun setupAllMenuRecycler() {
        allFoodItems = MyApplication.foodItems.toList()
        allMenuAdapter = FoodAdapter(allFoodItems.toMutableList(), this, R.layout.food_item_popular)

        findViewById<RecyclerView>(R.id.recyclerAllMenu).apply {
            isNestedScrollingEnabled = false
            layoutManager = GridLayoutManager(this@HomePageOrderActivity, 2)
            adapter = allMenuAdapter
            updateRecyclerViewHeight()
        }
    }

    private fun setupCategoriesRecycler() {
        val categories = listOf(
            CategoryItem("Main Meal", R.drawable.main_meal, Color.parseColor("#ffddc2")),
            CategoryItem("Snack & Side", R.drawable.snacks, Color.parseColor("#e4efdf")),
            CategoryItem("Beverage", R.drawable.beverages, Color.parseColor("#fdc7c4")),
            CategoryItem("Dessert", R.drawable.dessert, Color.parseColor("#c4c8fd"))
        )

        val popularNowContainer = findViewById<LinearLayout>(R.id.popularNowContainer)
        val popularNowLayout = findViewById<LinearLayout>(R.id.popularNowLayout)

        findViewById<RecyclerView>(R.id.categoryRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@HomePageOrderActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CategoryAdapter(categories) { selectedCategory ->
                filterFoodByCategory(selectedCategory)
                popularNowContainer.visibility = View.GONE
                popularNowLayout.visibility = View.GONE
            }
        }

        findViewById<TextView>(R.id.categoriesText).setOnClickListener {
            resetToDefaultView()
            popularNowContainer.visibility = View.VISIBLE
            popularNowLayout.visibility = View.VISIBLE
        }
    }

    private fun setupFoodItemsListener() {
        foodItemsListener = FirebaseRepository().listenForFoodItemsUpdates(
            onUpdate = { foodItems ->
                allFoodItems = foodItems.toList()
                popularFoodItems = foodItems.filter { it.isPopular }.toList()

                popularAdapter.updateFoodList(popularFoodItems)
                allMenuAdapter.updateFoodList(allFoodItems)

                resetToDefaultView()
                findViewById<LinearLayout>(R.id.popularNowContainer).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.popularNowLayout).visibility = View.VISIBLE
            },
            onFailure = { error ->
                Log.e("HomePageOrderActivity", "Error listening for food items", error)
            }
        )
    }

    private fun filterFoodByCategory(category: String) {
        val filteredList = if (category == "All") {
            allFoodItems
        } else {
            allFoodItems.filter { it.category.equals(category, ignoreCase = true) }
        }
        allMenuAdapter.updateFoodList(filteredList)
        findViewById<RecyclerView>(R.id.recyclerAllMenu).updateRecyclerViewHeight()
    }

    private fun resetToDefaultView() {
        allMenuAdapter.updateFoodList(allFoodItems)
        popularAdapter.updateFoodList(popularFoodItems)
        findViewById<RecyclerView>(R.id.recyclerAllMenu).updateRecyclerViewHeight()
    }

    private fun RecyclerView.updateRecyclerViewHeight() {
        post {
            val itemCount = adapter?.itemCount ?: 0
            if (itemCount > 0) {
                val child = getChildAt(0) ?: return@post
                val itemHeight = child.height
                val spanCount = (layoutManager as GridLayoutManager).spanCount
                val rows = ceil(itemCount.toDouble() / spanCount).toInt()

                val totalHeight = (rows * itemHeight) + (rows * 20)
                layoutParams.height = totalHeight + 100
                requestLayout()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        foodItemsListener?.remove()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_right)
    }
}