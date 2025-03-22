package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.FoodRepository
import cit.edu.wildcanteen.HomePageOrderActivity
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.FoodAdapter

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.homepage_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val makeOrderButton = view.findViewById<Button>(R.id.homepageOrder)
        makeOrderButton.setOnClickListener {
            startActivity(Intent(requireContext(), HomePageOrderActivity::class.java))
        }

        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        val foodList = FoodRepository.getPopularFoodList()
        val recyclerView: RecyclerView = view.findViewById(R.id.popularRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = FoodAdapter(foodList, requireContext(), R.layout.food_item)

        recyclerView.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(requireContext())
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
