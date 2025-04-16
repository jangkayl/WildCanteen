package cit.edu.wildcanteen.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.OrderBatchAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class DeliveryAssistanceActivity : AppCompatActivity() {
    private lateinit var firebaseRepository: FirebaseRepository
    private var orderBatchListener: ListenerRegistration? = null
    private lateinit var adapter: OrderBatchAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_batches)

        firebaseRepository = FirebaseRepository()
        recyclerView = findViewById(R.id.orderBatchesRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        loadDeliveryOrders()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = OrderBatchAdapter { batch ->
            val intent = Intent(this, OrderBatchDetailActivity::class.java).apply {
                putExtra("BATCH_ID", batch.batchId)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun loadDeliveryOrders() {
        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = null,
            onUpdate = { batches ->
                val deliveryOrders = batches.filter { batch ->
                    batch.status == "Ready" && batch.deliveryType == "Delivery"
                }.sortedByDescending { it.timestamp }

                adapter.submitList(deliveryOrders)
            },
            onFailure = { e ->
                Log.e("DeliveryAssistance", "Error loading delivery orders", e)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        orderBatchListener?.remove()
    }
}