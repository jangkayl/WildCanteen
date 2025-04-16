package cit.edu.wildcanteen.pages

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.DeliveryPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DeliveryAssistanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_assistance)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = DeliveryPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Available Orders"
                1 -> "Accepted Orders"
                else -> throw IllegalArgumentException("Invalid position")
            }
        }.attach()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}