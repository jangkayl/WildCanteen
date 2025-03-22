package cit.edu.wildcanteen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.FoodRepository
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CartAdapter
import cit.edu.wildcanteen.data_class.FoodItem

class OrdersFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var proceedButton: Button
    private val cartItems = mutableListOf<FoodItem>()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.food_cart, container, false)

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        proceedButton = view.findViewById(R.id.proceedButton)

        cartAdapter = CartAdapter(cartItems) { item: FoodItem -> removeItemFromCart(item) }
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartRecyclerView.adapter = cartAdapter

        loadCartItems()

        proceedButton.setOnClickListener {
            // Handle checkout logic
        }

        return view
    }

    private fun loadCartItems() {
        cartItems.addAll(FoodRepository.getPopularFoodList())
        cartAdapter.notifyDataSetChanged()
    }

    private fun removeItemFromCart(item: FoodItem) {
        cartItems.remove(item)
        cartAdapter.notifyDataSetChanged()
    }
}
