package cit.edu.wildcanteen.pages

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cit.edu.wildcanteen.CanteenOrderGroup
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.repositories.FirebaseRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderBatchDetailActivity : AppCompatActivity() {
    private lateinit var firebaseRepository: FirebaseRepository
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
        findViewById<LinearLayout>(R.id.orderItemsContainer).removeAllViews()
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

        Log.d("OrderBatchDetail", "Grouped orders count: ${batch.orders}")

        val orderedCanteenNames = batch.orders.map { it.canteenName }.distinct()

        val groupedOrders = orderedCanteenNames.map { canteenName ->
            val ordersForCanteen = batch.orders.filter { it.canteenName == canteenName }
            CanteenOrderGroup(canteenName, ordersForCanteen)
        }

        Log.d("OrderBatchDetail", "Grouped order list:")
        groupedOrders.forEach {
            Log.d("OrderBatchDetail", "Canteen: ${it.canteenName}, Items: ${it.orders.size}")
        }

        Log.d("OrderBatchDetail", "Grouped orders count: ${groupedOrders.size}")
        groupedOrders.forEach { group ->
            Log.d("OrderBatchDetail", "Canteen: ${group.orders}}, Items: ${group.orders.size}")
        }

        displayGroupedOrders(groupedOrders)
    }

    private fun displayGroupedOrders(groupedOrders: List<CanteenOrderGroup>) {
        val orderItemsContainer = findViewById<LinearLayout>(R.id.orderItemsContainer)
        orderItemsContainer.removeAllViews()

        for (group in groupedOrders) {
            try {
                Log.d("OrderBatchDetail", "Adding canteen header for: ${group.canteenName ?: "Unknown"}")

                val headerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.small_margin))
                }

                val canteenHeader = layoutInflater.inflate(R.layout.item_canteen_header, null).apply {
                    layoutParams = headerParams
                    findViewById<TextView>(R.id.canteenNameText).text = group.canteenName.takeIf { it.isNotBlank() } ?: "Unknown Canteen"
                }
                orderItemsContainer.addView(canteenHeader)

                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.divider_height)
                    ).apply {
                        setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.small_margin))
                    }
                    setBackgroundColor(ContextCompat.getColor(this@OrderBatchDetailActivity, R.color.gray))
                }
                orderItemsContainer.addView(divider)

                val itemParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.small_margin))
                }

                for (order in group.orders) {
                    val orderItemView = layoutInflater.inflate(R.layout.item_order, null).apply {
                        layoutParams = itemParams
                        findViewById<TextView>(R.id.itemNameText).text = order.items.name
                        findViewById<TextView>(R.id.itemQuantityText).text = "x${order.quantity}"
                        findViewById<TextView>(R.id.itemPriceText).text = "₱${"%.2f".format(order.items.price * order.quantity)}"
                    }
                    orderItemsContainer.addView(orderItemView)
                }

                if (group != groupedOrders.last()) {
                    val spacer = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            resources.getDimensionPixelSize(R.dimen.medium_margin)
                        )
                    }
                    orderItemsContainer.addView(spacer)
                }

            } catch (e: Exception) {
                Log.e("OrderBatchDetail", "Error displaying canteen group: ${e.message}", e)
                Toast.makeText(this, "Error displaying order details", Toast.LENGTH_SHORT).show()
            }
        }
    }

}