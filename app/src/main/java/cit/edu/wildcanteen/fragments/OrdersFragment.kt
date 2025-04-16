package cit.edu.wildcanteen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.OrdersPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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