package cit.edu.wildcanteen.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cit.edu.wildcanteen.fragments.CartFragment
import cit.edu.wildcanteen.fragments.OrderHistoryFragment
import cit.edu.wildcanteen.fragments.OrderedFragment

class OrdersPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CartFragment()
            1 -> OrderedFragment()
            2 -> OrderHistoryFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}