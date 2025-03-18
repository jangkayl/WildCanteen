package cit.edu.wildcanteen

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.ceil

class HomePageOrderActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_main)
        setupRecyclerView()
        setupCategories()
        setupAllMenu()
    }

    private fun setupRecyclerView() {
        val foodList = FoodRepository.getPopularFoodList()
        val recyclerView: RecyclerView = findViewById(R.id.popularNowRecycleView)
        recyclerView.layoutManager =
            LinearLayoutManager(this@HomePageOrderActivity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = FoodAdapter(foodList, this, R.layout.food_item_popular)
    }

    private fun setupCategories() {
        val categories = listOf(
            CategoryItem("Main Meal", R.drawable.main_meal, Color.parseColor("#ffddc2")),
            CategoryItem("Snacks & Sides", R.drawable.snacks, Color.parseColor("#e4efdf")),
            CategoryItem("Beverages", R.drawable.beverages, Color.parseColor("#fdc7c4")),
            CategoryItem("Dessert", R.drawable.dessert, Color.parseColor("#c4c8fd"))
        )

        val recyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = CategoryAdapter(categories)
    }

    private fun setupAllMenu() {
        val foodList = FoodRepository.getAllFoodList()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAllMenu)

        val gridLayoutManager = GridLayoutManager(this, 2)

        recyclerView.apply {
            isNestedScrollingEnabled = false
            layoutManager = gridLayoutManager
            adapter = FoodAdapter(foodList, this@HomePageOrderActivity, R.layout.food_item_popular)

            post {
                val itemCount = adapter?.itemCount ?: 0
                if (itemCount > 0) {
                    val child = getChildAt(0) ?: return@post
                    val itemHeight = child.measuredHeight
                    val spanCount = gridLayoutManager.spanCount
                    val rows = ceil(itemCount / spanCount.toDouble()).toInt()

                    val totalHeight = (rows * itemHeight) + (rows * 20)
                    layoutParams.height = totalHeight + 100
                    requestLayout()
                }
            }
        }
    }
}
