package cit.edu.wildcanteen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomePageActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)
        setupNavigation()
        setupRecyclerView()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.home_button).setOnClickListener {
            findViewById<ScrollView>(R.id.homeScrollView).smoothScrollTo(0, 0)
        }

        findViewById<View>(R.id.homepageOrder).setOnClickListener {
            startActivity(Intent(this, HomePageOrderActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val foodList = FoodRepository.getPopularFoodList()
        val recyclerView: RecyclerView = findViewById(R.id.popularRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = FoodAdapter(foodList, this, R.layout.food_item)

        recyclerView.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(this@HomePageActivity)
            setAdapter(adapter)

            post {
                var totalHeight = 0
                for (i in 0 until adapter.itemCount) {
                    val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
                    adapter.bindViewHolder(holder, i)

                    holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )

                    val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                    totalHeight += holder.itemView.measuredHeight + params.bottomMargin
                }

                layoutParams.height = totalHeight
                requestLayout()
            }
        }
    }
}
