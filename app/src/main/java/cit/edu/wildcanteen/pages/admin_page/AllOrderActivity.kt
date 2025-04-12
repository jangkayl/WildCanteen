package cit.edu.wildcanteen.pages.admin_page

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class AllOrderActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var noOrderLayout: LinearLayout

    private val cartOrders = mutableListOf<Order>()
    private lateinit var cartAdapter: AllOrderAdapter
    private var orderListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_orders_page)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        backButton = findViewById(R.id.backButton)
        noOrderLayout = findViewById(R.id.no_order_layout)

        cartAdapter = AllOrderAdapter(this, cartOrders){}
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter

        backButton.setOnClickListener {
            finish()
        }

        loadAllOrders()
        startListeningForAllOrderUpdates()
    }

    private fun startListeningForAllOrderUpdates() {
        orderListener = FirebaseRepository().listenForAllOrderUpdates { updatedOrders ->
            cartOrders.clear()
            cartOrders.addAll(updatedOrders)
            updateUI()
        }
    }

    private fun loadAllOrders() {
        FirebaseRepository().getAllOrders(
            onSuccess = { orders ->
                cartOrders.clear()
                cartOrders.addAll(orders)
                updateUI()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Failed to load orders: ${exception.message}", Toast.LENGTH_SHORT).show()
                updateUI()
            }
        )
    }

    private fun updateUI() {
        val isCartEmpty = cartOrders.isEmpty()
        cartRecyclerView.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
        noOrderLayout.visibility = if (isCartEmpty) View.VISIBLE else View.GONE
        cartAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        loadAllOrders()
    }

    override fun onDestroy() {
        super.onDestroy()
        orderListener?.remove()
        orderListener = null
    }
}