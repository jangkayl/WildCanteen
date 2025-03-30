package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.AddFoodItemActivity
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.HomePageOrderActivity
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.FoodAdapter
import cit.edu.wildcanteen.application.MyApplication
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class HomeFragment : Fragment() {
    private var foodList: List<FoodItem> = MyApplication.popularFoodItems
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.homepage_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val makeOrderButton = view.findViewById<Button>(R.id.homepageOrder)
        val usernameTextView = view.findViewById<TextView>(R.id.username)
        val userIdTextView = view.findViewById<TextView>(R.id.user_id)
        val profileImage = view.findViewById<ImageView>(R.id.user_profile)
        val notification = view.findViewById<ImageView>(R.id.notification)

        usernameTextView.text = MyApplication.name
        userIdTextView.text = MyApplication.stringStudentId

        if (!MyApplication.profileImageUrl.isNullOrEmpty()) {
            Log.e("ProfileImage", MyApplication.profileImageUrl!!)
            Glide.with(this)
                .load(MyApplication.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .into(profileImage)
        }

        makeOrderButton.setOnClickListener {
            startActivity(Intent(requireContext(), HomePageOrderActivity::class.java))
        }

        notification.setOnClickListener {
            startActivity(Intent(requireContext(), AddFoodItemActivity::class.java))
        }

        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        adapter = FoodAdapter(MyApplication.popularFoodItems.toMutableList(), requireContext(), R.layout.food_item)

        val recyclerView: RecyclerView = view.findViewById(R.id.popularRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter

        recyclerView.post {
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

            recyclerView.layoutParams.height = totalHeight
            recyclerView.requestLayout()
        }
    }

    override fun onResume() {
        super.onResume()

        MyApplication.loadFoodItems {
            val updatedFoodList = MyApplication.popularFoodItems.toMutableList()

            if (updatedFoodList != foodList) {
                foodList = updatedFoodList
                adapter.updateFoodList(foodList)
                adapter.notifyDataSetChanged()
            }
        }
    }
}



