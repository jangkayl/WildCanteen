package cit.edu.wildcanteen.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CartAdapter
import cit.edu.wildcanteen.application.MyApplication

class OrdersFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var proceedButton: Button
    private lateinit var proceedContainer: LinearLayout
    private lateinit var subtotalAmount: TextView
    private lateinit var chargeAmount: TextView
    private lateinit var discountAmount: TextView
    private lateinit var totalAmount: TextView
    private val cartOrders = mutableListOf<Order>()
    private lateinit var cartAdapter: CartAdapter
    private var isProceedButtonClicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.food_cart, container, false)

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        proceedButton = view.findViewById(R.id.proceedButton)
        proceedContainer = view.findViewById(R.id.proceedContainer)
        subtotalAmount = view.findViewById(R.id.subtotal_amount)
        chargeAmount = view.findViewById(R.id.charge_amount)
        discountAmount = view.findViewById(R.id.discount_amount)
        totalAmount = view.findViewById(R.id.total_amount)

        cartAdapter = CartAdapter(requireContext(), cartOrders, ::removeOrderFromCart, ::updateTotalAmount)
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartRecyclerView.adapter = cartAdapter

        loadCartOrders()
        updateTotalAmount()

        proceedButton.setOnClickListener {
            if (proceedContainer.visibility == View.GONE) {
                isProceedButtonClicked = true
                proceedContainer.visibility = View.VISIBLE
                proceedContainer.alpha = 0f
                proceedContainer.translationY = 100f

                proceedContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .withEndAction { isProceedButtonClicked = false }
                    .start()

                proceedButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(android.R.color.white))
                proceedButton.setTextColor(resources.getColor(R.color.selectedColor))
            } else {
                isProceedButtonClicked = false
            }
        }

        proceedContainer.setOnClickListener {
            proceedContainer.animate()
                .alpha(0f)
                .translationY(100f)
                .setDuration(400)
                .withEndAction { proceedContainer.visibility = View.GONE }
                .start()

            proceedButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.selectedColor))
            proceedButton.setTextColor(resources.getColor(android.R.color.white))
        }

        return view
    }

    private fun loadCartOrders() {
        cartOrders.clear()
        cartOrders.addAll(MyApplication.orders.reversed())
        cartAdapter.notifyDataSetChanged()
    }

    private fun removeOrderFromCart(order: Order) {
        cartOrders.remove(order)
        MyApplication.orders.remove(order)
        MyApplication.saveOrders()
        cartAdapter.notifyDataSetChanged()
        updateTotalAmount()
    }

    private fun updateTotalAmount() {
        val charge = 10.0
        val discount = 5.0
        val subtotal = MyApplication.orders.sumOf { it.totalAmount }

        subtotalAmount.text = String.format("₱%.2f", subtotal)
        chargeAmount.text = String.format("₱%.2f", charge)
        discountAmount.text = String.format("-₱%.2f", discount)
        totalAmount.text = String.format("₱%.2f", subtotal + charge - discount)
    }
}