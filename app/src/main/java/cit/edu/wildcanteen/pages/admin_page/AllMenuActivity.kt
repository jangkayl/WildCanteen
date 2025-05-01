package cit.edu.wildcanteen.pages.admin_page

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.CategoryItem
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CategoryAdapter
import cit.edu.wildcanteen.adapters.FoodAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.ceil

class AllMenuActivity : Activity() {
    private var foodItemsListener: ListenerRegistration? = null
    private lateinit var popularAdapter: FoodAdapter
    private lateinit var allMenuAdapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.admin_all_menu)

        findViewById<LinearLayout>(R.id.logoBack).setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupAllMenu()
        setupFoodItemsListener()
    }

    private fun setupRecyclerView() {
        val foodList = MyApplication.popularFoodItems
        val recyclerView: RecyclerView = findViewById(R.id.popularNowRecycleView)
        recyclerView.layoutManager =
            LinearLayoutManager(this@AllMenuActivity, LinearLayoutManager.HORIZONTAL, false)
        popularAdapter = FoodAdapter(foodList, this, R.layout.food_item_popular)
        recyclerView.adapter = popularAdapter
    }

    private fun setupAllMenu() {
        val foodList = MyApplication.foodItems
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAllMenu)

        val gridLayoutManager = GridLayoutManager(this, 2)
        allMenuAdapter = FoodAdapter(foodList, this@AllMenuActivity, R.layout.food_item_popular)

        recyclerView.apply {
            isNestedScrollingEnabled = false
            layoutManager = gridLayoutManager
            adapter = allMenuAdapter

            updateRecyclerViewHeight()
        }
    }

    private fun setupFoodItemsListener() {
        foodItemsListener = FirebaseRepository().listenForFoodItemsUpdates(
            onUpdate = { foodItems ->
                MyApplication.popularFoodItems = foodItems.filter { it.isPopular }.toMutableList()
                MyApplication.foodItems = foodItems.toMutableList()

                popularAdapter.updateFoodList(MyApplication.popularFoodItems)
                allMenuAdapter.updateFoodList(MyApplication.foodItems)

                popularAdapter.notifyDataSetChanged()
                allMenuAdapter.notifyDataSetChanged()

                findViewById<RecyclerView>(R.id.recyclerAllMenu).post {
                    updateRecyclerViewHeight()
                }
            },
            onFailure = { error ->
                Log.e("AllMenuActivity", "Error listening for food items", error)
            }
        )
    }

    private fun updateRecyclerViewHeight() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAllMenu)
        val itemCount = allMenuAdapter.itemCount
        if (itemCount > 0) {
            val child = recyclerView.getChildAt(0) ?: return
            val itemHeight = child.measuredHeight
            val spanCount = (recyclerView.layoutManager as GridLayoutManager).spanCount
            val rows = ceil(itemCount.toDouble() / spanCount).toInt()

            val totalHeight = (rows * itemHeight) + (rows * 20)
            recyclerView.layoutParams.height = totalHeight + 100
            recyclerView.requestLayout()
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