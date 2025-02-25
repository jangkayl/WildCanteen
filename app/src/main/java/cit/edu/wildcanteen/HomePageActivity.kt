package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomePageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val settingsButton = findViewById<ImageView>(R.id.settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val homeButton = findViewById<ImageView>(R.id.home_button)
        val scrollView = findViewById<ScrollView>(R.id.homeScrollView)

        homeButton.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }

        val orderHomePage = findViewById<View>(R.id.homepageOrder)
        orderHomePage.setOnClickListener {
            val intent = Intent(this, HomePageOrderActivity::class.java)
            startActivity(intent)
        }

        // Dynamic Most Popular
        val foodList = listOf(
            FoodItem("Palabok", "₱80.00", "4.9", R.drawable.palabok),
            FoodItem("Lumpia", "₱50.00", "4.6", R.drawable.lumpia)
        )

        val recyclerView: RecyclerView = findViewById(R.id.popularRecyclerView)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FoodAdapter(foodList)
    }
}
