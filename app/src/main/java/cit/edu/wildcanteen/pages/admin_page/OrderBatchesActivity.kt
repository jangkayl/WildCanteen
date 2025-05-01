package cit.edu.wildcanteen.pages.admin_page

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.AdminOrderPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OrderBatchesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_batches)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = AdminOrderPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Preparing"
                1 -> "Delivering/Ready"
                else -> "Completed/Cancelled"
            }
        }.attach()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}

