package cit.edu.wildcanteen

import android.app.Activity
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
        val foodList = listOf(
            FoodItem("Inasal Chicken", "₱99.00", "4.8", "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Palabok", "₱80.00", "4.9", "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Lumpia", "₱50.00", "4.6", "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia)
        )

        val recyclerView: RecyclerView = findViewById(R.id.popularRecyclerView)
        val adapter = FoodAdapter(foodList, this)

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
