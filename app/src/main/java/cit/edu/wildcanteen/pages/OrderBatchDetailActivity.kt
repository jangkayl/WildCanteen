package cit.edu.wildcanteen.pages

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
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

    override fun onDestroy() {
        super.onDestroy()
        adapter.submitList(emptyList())
    }

    private fun setupViews() {
        adapter = OrderItemAdapter()
        findViewById<RecyclerView>(R.id.orderItemsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@OrderBatchDetailActivity)
            adapter = this@OrderBatchDetailActivity.adapter

            val divider = DividerItemDecoration(this@OrderBatchDetailActivity, DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(this@OrderBatchDetailActivity, R.drawable.divider)?.let {
                divider.setDrawable(it)
            }
            addItemDecoration(divider)
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadOrderBatchDetails() {
        resetUI()

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

    private fun resetUI() {
        findViewById<TextView>(R.id.batchIdText).text = ""
        findViewById<TextView>(R.id.customerNameText).text = ""
        findViewById<TextView>(R.id.statusText).text = ""
        findViewById<TextView>(R.id.dateText).text = ""
        findViewById<TextView>(R.id.paymentMethodText).text = ""
        findViewById<TextView>(R.id.deliveryTypeText).text = ""
        findViewById<TextView>(R.id.totalAmountText).text = ""
        findViewById<TextView>(R.id.subtotalText).text = ""
        findViewById<TextView>(R.id.deliveryFeeText).text = ""
        findViewById<TextView>(R.id.referenceNumberText).visibility = View.GONE
        findViewById<TextView>(R.id.deliveryAddressText).visibility = View.GONE
        adapter.submitList(emptyList())
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
        findViewById<TextView>(R.id.subtotalText).text = "₱${"%.2f".format(batch.totalAmount + 5 - batch.deliveryFee)}"
        findViewById<TextView>(R.id.deliveryFeeText).text = "₱${"%.2f".format(batch.deliveryFee)}"

        if (batch.paymentMethod.equals("GCash", ignoreCase = true) && !batch.referenceNumber.isNullOrBlank()) {
            findViewById<TextView>(R.id.referenceNumberText).apply {
                visibility = View.VISIBLE
                text = "Ref: ${batch.referenceNumber}"
            }
        }

        if (batch.paymentMethod.equals("Cash", ignoreCase = true) && !batch.referenceNumber.isNullOrBlank()) {
            findViewById<TextView>(R.id.referenceNumberText).apply {
                visibility = View.VISIBLE
                text = "Amount: ${batch.referenceNumber}"
            }
        }

        if (batch.deliveryType.equals("Delivery", ignoreCase = true) && !batch.deliveryAddress.isNullOrBlank()) {
            findViewById<LinearLayout>(R.id.deliveryLayout).apply {
                visibility = View.VISIBLE
            }
            findViewById<TextView>(R.id.deliveryAddressText).apply {
                visibility = View.VISIBLE
                text = batch.deliveryAddress
            }
        }

        when (batch.status.lowercase()) {
            "pending" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#FFA500"))
            "preparing" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#2196F3"))
            "ready" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#4CAF50"))
            "completed" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#607D8B"))
            "cancelled" -> findViewById<TextView>(R.id.statusText).setTextColor(Color.parseColor("#F44336"))
        }

        adapter.submitList(batch.orders)
    }
}