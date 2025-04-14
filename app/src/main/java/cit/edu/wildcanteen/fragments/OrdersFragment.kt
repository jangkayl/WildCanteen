package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CartAdapter
import cit.edu.wildcanteen.adapters.OrdersPagerAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.HomePageActivity
import cit.edu.wildcanteen.pages.OrderSummaryActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ListenerRegistration

class OrdersFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var ordersPagerAdapter: OrdersPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders_tabs, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        setupViewPager()

        return view
    }

    private fun setupViewPager() {
        ordersPagerAdapter = OrdersPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = ordersPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Cart"
                1 -> tab.text = "Ordered"
                2 -> tab.text = "History"
            }
        }.attach()
    }
}