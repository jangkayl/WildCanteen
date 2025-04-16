package cit.edu.wildcanteen.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cit.edu.wildcanteen.fragments.AcceptedOrdersFragment
import cit.edu.wildcanteen.fragments.AvailableOrdersFragment

class DeliveryPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> AvailableOrdersFragment()
            1 -> AcceptedOrdersFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}