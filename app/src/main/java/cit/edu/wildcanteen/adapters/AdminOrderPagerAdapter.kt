package cit.edu.wildcanteen.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cit.edu.wildcanteen.fragments.OrderBatchFragment

class AdminOrderPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OrderBatchFragment.newInstance("preparing")
            1 -> OrderBatchFragment.newInstance("delivering_ready")
            else -> OrderBatchFragment.newInstance("completed_cancelled")
        }
    }
}