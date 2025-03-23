package cit.edu.wildcanteen.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CartAdapter
import cit.edu.wildcanteen.application.MyApplication

class OrdersFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var proceedButton: Button
    private val cartOrders = mutableListOf<Order>()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.food_cart, container, false)

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        proceedButton = view.findViewById(R.id.proceedButton)

        cartAdapter = CartAdapter(cartOrders) { order: Order -> removeOrderFromCart(order) }
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartRecyclerView.adapter = cartAdapter

        loadCartOrders()

        proceedButton.setOnClickListener {
            // Handle checkout logic
        }

        return view
    }

    private fun loadCartOrders() {
        cartOrders.clear()
        cartOrders.addAll(MyApplication.orders)
        cartAdapter.notifyDataSetChanged()
    }

    private fun removeOrderFromCart(order: Order) {
        cartOrders.remove(order)
        MyApplication.orders.remove(order)
        MyApplication.saveOrders()
        cartAdapter.notifyDataSetChanged()
    }
}

