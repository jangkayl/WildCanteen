package cit.edu.wildcanteen.pages.student_pages

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
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
import kotlin.math.ceil

class HomePageOrderActivity : Activity() {
    private lateinit var allFoodItems: List<FoodItem>
    private lateinit var popularAdapter: FoodAdapter
    private lateinit var allMenuAdapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.homepage_main)

        allFoodItems = MyApplication.foodItems
        val popularItems = MyApplication.popularFoodItems

        findViewById<LinearLayout>(R.id.logoBack).setOnClickListener { finish() }

        setupPopularItemsRecycler(popularItems)
        setupCategoriesRecycler()
        setupAllMenuRecycler(allFoodItems)
    }

    private fun setupPopularItemsRecycler(popularItems: List<FoodItem>) {
        popularAdapter = FoodAdapter(popularItems.toMutableList(), this, R.layout.food_item_popular)
        findViewById<RecyclerView>(R.id.popularNowRecycleView).apply {
            layoutManager = LinearLayoutManager(
                this@HomePageOrderActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = popularAdapter
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
            layoutManager = LinearLayoutManager(
                this@HomePageOrderActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = CategoryAdapter(categories) { selectedCategory ->
                filterFoodByCategory(selectedCategory)
                popularNowContainer?.visibility = View.GONE
                popularNowLayout?.visibility = View.GONE
            }
        }

        findViewById<TextView>(R.id.categoriesText).setOnClickListener {
            popularNowContainer?.visibility = View.VISIBLE
            popularNowLayout?.visibility = View.VISIBLE

            allMenuAdapter.updateFoodList(allFoodItems)
            popularAdapter.updateFoodList(allFoodItems.filter { it.isPopular })

            findViewById<RecyclerView>(R.id.recyclerAllMenu).updateRecyclerViewHeight()
        }
    }

    private fun setupAllMenuRecycler(foodItems: List<FoodItem>) {
        allMenuAdapter = FoodAdapter(foodItems.toMutableList(), this, R.layout.food_item_popular)
        val gridLayoutManager = GridLayoutManager(this, 2)

        findViewById<RecyclerView>(R.id.recyclerAllMenu).apply {
            isNestedScrollingEnabled = false
            layoutManager = gridLayoutManager
            adapter = allMenuAdapter
            updateRecyclerViewHeight()
        }
    }

    private fun filterFoodByCategory(category: String) {
        val filteredList = if (category == "All") {
            allFoodItems
        } else {
            allFoodItems.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Update both adapters
        allMenuAdapter.updateFoodList(filteredList)
        popularAdapter.updateFoodList(filteredList.filter { it.isPopular })

        // Update RecyclerView height
        findViewById<RecyclerView>(R.id.recyclerAllMenu).updateRecyclerViewHeight()
    }

    private fun RecyclerView.updateRecyclerViewHeight() {
        post {
            val itemCount = adapter?.itemCount ?: 0
            if (itemCount > 0) {
                val child = getChildAt(0) ?: return@post
                val itemHeight = child.measuredHeight
                val spanCount = (layoutManager as GridLayoutManager).spanCount
                val rows = ceil(itemCount.toDouble() / spanCount).toInt()

                val totalHeight = (rows * itemHeight) + (rows * 20) // Add some padding
                layoutParams.height = totalHeight + 100
                requestLayout()
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_right)
    }
}