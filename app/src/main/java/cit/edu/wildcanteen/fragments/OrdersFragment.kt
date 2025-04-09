package cit.edu.wildcanteen.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.CartAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ListenerRegistration

class OrdersFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var proceedButton: Button
    private lateinit var proceedContainer: LinearLayout
    private lateinit var subtotalAmount: TextView
    private lateinit var chargeAmount: TextView
    private lateinit var discountAmount: TextView
    private lateinit var totalAmount: TextView
    private lateinit var noOrderLayout: LinearLayout
    private lateinit var exploreButton: Button

    private val cartOrders = mutableListOf<Order>()
    private lateinit var cartAdapter: CartAdapter
    private var isProceedButtonClicked = false
    private var orderListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.food_cart, container, false)

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        proceedButton = view.findViewById(R.id.proceedButton)
        proceedContainer = view.findViewById(R.id.proceedContainer)
        subtotalAmount = view.findViewById(R.id.subtotal_amount)
        chargeAmount = view.findViewById(R.id.charge_amount)
        discountAmount = view.findViewById(R.id.discount_amount)
        totalAmount = view.findViewById(R.id.total_amount)
        noOrderLayout = view.findViewById(R.id.no_order_layout)
        exploreButton = view.findViewById(R.id.explore_button)

        cartAdapter = CartAdapter(requireContext(), cartOrders, ::removeOrderFromCart, ::updateTotalAmount)
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartRecyclerView.adapter = cartAdapter

        loadCartOrders()
        updateTotalAmount()

        proceedButton.setOnClickListener { toggleProceedContainer() }
        proceedContainer.setOnClickListener { hideProceedContainer() }

        exploreButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.linear_container, HomeFragment())
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startListeningForOrderUpdates()
    }

    private fun startListeningForOrderUpdates() {
        val userId = MyApplication.studentId ?: return

        orderListener = FirebaseRepository().listenForOrderUpdates(userId) { updatedOrders ->
            MyApplication.orders.clear()
            MyApplication.orders.addAll(updatedOrders)

            loadCartOrders()
            updateTotalAmount()
        }
    }

    private fun toggleProceedContainer() {
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

            proceedButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))
            proceedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.selectedColor))
        }
    }

    private fun hideProceedContainer() {
        proceedContainer.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(400)
            .withEndAction { proceedContainer.visibility = View.GONE }
            .start()

        proceedButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.selectedColor))
        proceedButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun loadCartOrders() {
        cartOrders.clear()
        cartOrders.addAll(MyApplication.orders.reversed())

        val isCartEmpty = cartOrders.isEmpty()

        cartRecyclerView.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
        noOrderLayout.visibility = if (isCartEmpty) View.VISIBLE else View.GONE
        proceedButton.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
        proceedContainer.visibility = if (isCartEmpty) View.GONE else proceedContainer.visibility

        cartAdapter.notifyDataSetChanged()
    }


    private fun removeOrderFromCart(order: Order) {
        MyApplication.orders.remove(order)
        MyApplication.saveOrders(emptyList())
        cartAdapter.notifyDataSetChanged()
        loadCartOrders()
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

        if (MyApplication.orders.isEmpty()) loadCartOrders()
        cartAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        loadCartOrders()
        updateTotalAmount()
        cartAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListener?.remove()
        orderListener = null
    }
}
