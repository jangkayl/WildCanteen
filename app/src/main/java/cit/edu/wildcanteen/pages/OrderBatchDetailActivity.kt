package cit.edu.wildcanteen.pages

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderBatchDetailActivity : AppCompatActivity() {
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var adapter: OrderItemAdapter
    private lateinit var batchId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_batch_detail)

        firebaseRepository = FirebaseRepository()
        batchId = intent.getStringExtra("BATCH_ID") ?: run {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadOrderBatchDetails()
    }

    private fun setupViews() {
        adapter = OrderItemAdapter()
        findViewById<RecyclerView>(R.id.orderItemsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@OrderBatchDetailActivity)
            adapter = this@OrderBatchDetailActivity.adapter
            addItemDecoration(DividerItemDecoration(this@OrderBatchDetailActivity, DividerItemDecoration.VERTICAL))
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadOrderBatchDetails() {
        firebaseRepository.getOrderBatches(
            onSuccess = { batches ->
                val batch = batches.firstOrNull { it.batchId == batchId } ?: run {
                    Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@getOrderBatches
                }

                updateUI(batch)
            },
            onFailure = { e ->
                Toast.makeText(this, "Failed to load order: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    private fun updateUI(batch: OrderBatch) {
        findViewById<TextView>(R.id.batchIdText).text = "Order #${batch.batchId.takeLast(6)}"
        findViewById<TextView>(R.id.customerNameText).text = batch.userName
        findViewById<TextView>(R.id.statusText).text = batch.status
        findViewById<TextView>(R.id.dateText).text = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            .format(Date(batch.timestamp))
        findViewById<TextView>(R.id.paymentMethodText).text = batch.paymentMethod
        findViewById<TextView>(R.id.deliveryTypeText).text = batch.deliveryType
        findViewById<TextView>(R.id.totalAmountText).text = "₱${"%.2f".format(batch.totalAmount)}"

        // Set status color
        when (batch.status.lowercase()) {
            "pending" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#FFA500"))
            "preparing" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#2196F3"))
            "ready" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#4CAF50"))
            "completed" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#607D8B"))
            "cancelled" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#F44336"))
        }

        adapter.submitList(batch.orders)

        // Show action buttons for admin
        if (MyApplication.userType == "admin") {
            findViewById<LinearLayout>(R.id.adminActionsLayout).visibility = View.VISIBLE

            findViewById<Button>(R.id.btnUpdateStatus).setOnClickListener {
                showStatusUpdateDialog(batch)
            }
        } else {
            findViewById<LinearLayout>(R.id.adminActionsLayout).visibility = View.GONE
        }
    }

    private fun showStatusUpdateDialog(batch: OrderBatch) {
        val statuses = arrayOf("Pending", "Preparing", "Ready", "Completed", "Cancelled")
        val currentIndex = statuses.indexOfFirst { it.equals(batch.status, ignoreCase = true) }

        AlertDialog.Builder(this)
            .setTitle("Update Order Status")
            .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                val newStatus = statuses[which]
                updateBatchStatus(batch.batchId, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBatchStatus(batchId: String, newStatus: String) {
        val updates = mapOf("status" to newStatus)

        firebaseRepository.db.collection("order_batches").document(batchId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}