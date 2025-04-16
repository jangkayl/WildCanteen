package cit.edu.wildcanteen.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.DeliveryOrderAdapter
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class DeliveryAssistanceActivity : AppCompatActivity() {
    private lateinit var firebaseRepository: FirebaseRepository
    private var orderBatchListener: ListenerRegistration? = null
    private lateinit var adapter: DeliveryOrderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noOrderLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_assistance)

        firebaseRepository = FirebaseRepository()
        recyclerView = findViewById(R.id.deliveryOrdersRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        noOrderLayout = findViewById(R.id.no_order_layout)

        setupRecyclerView()
        loadDeliveryOrders()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = DeliveryOrderAdapter(
            onItemClick = { batch ->
                showOrderDetails(batch)
            },
            onAcceptClick = { batch ->
                acceptDelivery(batch)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun showOrderDetails(batch: OrderBatch) {
        val intent = Intent(this, OrderBatchDetailActivity::class.java).apply {
            putExtra("BATCH_ID", batch.batchId)
        }
        startActivity(intent)
    }

    private fun acceptDelivery(batch: OrderBatch) {
        // Update order status to "On Delivery" and assign delivery person
//        firebaseRepository.acceptDeliveryOrder(batch.batchId) { success, e ->
//            if (success) {
//                // Show success message
//                showToast("Delivery accepted! You'll earn ₱${batch.deliveryFee}")
//            } else {
//                Log.e("DeliveryAssistance", "Error accepting delivery", e)
//                showToast("Failed to accept delivery")
//            }
//        }
    }

    private fun loadDeliveryOrders() {
        progressBar.visibility = View.VISIBLE
        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = null,
            onUpdate = { batches ->
                progressBar.visibility = View.GONE
                val deliveryOrders = batches.filter { batch ->
                    batch.status == "Ready" && batch.deliveryType == "Delivery"
                }.sortedByDescending { it.timestamp }

                adapter.submitList(deliveryOrders)

                if (deliveryOrders.isEmpty()) {
                    noOrderLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    noOrderLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            },
            onFailure = { e ->
                progressBar.visibility = View.GONE
                Log.e("DeliveryAssistance", "Error loading delivery orders", e)
                noOrderLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        )
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        orderBatchListener?.remove()
    }
}