package cit.edu.wildcanteen.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository

class OrderSummaryActivity : AppCompatActivity() {

    private lateinit var deliveryFeeTextView: TextView
    private lateinit var discountTextView: TextView
    private lateinit var totalPriceTextView: TextView
    private lateinit var orderItemsContainer: LinearLayout
    private lateinit var deliveryMethodGroup: RadioGroup
    private lateinit var paymentMethodGroup: RadioGroup
    private lateinit var cashInputContainer: LinearLayout
    private lateinit var gcashInputContainer: LinearLayout
    private lateinit var deliveryAddressContainer: LinearLayout
    private lateinit var deliveryAddressInput: EditText
    private lateinit var cashAmountInput: EditText
    private lateinit var gcashRefInput: EditText
    private lateinit var payNowButton: Button

    private val cartOrders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_summary_page)
        cartOrders.addAll(MyApplication.orders.reversed())

        val backButton: ImageView = findViewById(R.id.btn_back)
        payNowButton = findViewById(R.id.btn_pay_now)
        deliveryFeeTextView = findViewById(R.id.deliveryFee)
        totalPriceTextView = findViewById(R.id.totalPrice)
        orderItemsContainer = findViewById(R.id.orderItemsContainer)
        deliveryMethodGroup = findViewById(R.id.deliveryMethodGroup)
        discountTextView = findViewById(R.id.discount)
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup)

        cashInputContainer = findViewById(R.id.cashInputContainer)
        gcashInputContainer = findViewById(R.id.gcashInputContainer)
        deliveryAddressContainer = findViewById(R.id.deliveryAddressContainer)
        deliveryAddressInput = findViewById(R.id.deliveryAddressInput)
        cashAmountInput = findViewById(R.id.cashAmountInput)
        gcashRefInput = findViewById(R.id.gcashRefInput)

        populateOrderItems()
        updateTotalPrice()

        findViewById<LinearLayout>(R.id.onSiteContainer).setOnClickListener {
            deliveryMethodGroup.check(R.id.onSiteRadio)
            updateDeliveryFee(R.id.onSiteRadio)
            deliveryAddressContainer.visibility = View.GONE
        }

        findViewById<LinearLayout>(R.id.deliveryContainer).setOnClickListener {
            deliveryMethodGroup.check(R.id.deliveryRadio)
            updateDeliveryFee(R.id.deliveryRadio)
            deliveryAddressContainer.visibility = View.VISIBLE
        }

        findViewById<LinearLayout>(R.id.cashContainer).setOnClickListener {
            paymentMethodGroup.check(R.id.cashRadio)
        }

        findViewById<LinearLayout>(R.id.gcashContainer).setOnClickListener {
            paymentMethodGroup.check(R.id.gcashRadio)
        }

        deliveryMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.deliveryRadio -> {
                    deliveryAddressContainer.visibility = View.VISIBLE
                    updateDeliveryFee(checkedId)
                }
                else -> {
                    deliveryAddressContainer.visibility = View.GONE
                    updateDeliveryFee(checkedId)
                }
            }
        }

        paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.cashRadio -> {
                    cashInputContainer.visibility = View.VISIBLE
                    gcashInputContainer.visibility = View.GONE
                }
                R.id.gcashRadio -> {
                    cashInputContainer.visibility = View.GONE
                    gcashInputContainer.visibility = View.VISIBLE
                }
                else -> {
                    cashInputContainer.visibility = View.GONE
                    gcashInputContainer.visibility = View.GONE
                }
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        payNowButton.setOnClickListener {
            payNowButton.isEnabled = false

            val deliveryMethod = when (deliveryMethodGroup.checkedRadioButtonId) {
                R.id.onSiteRadio -> "On-Site Pickup"
                else -> "Delivery"
            }

            val total = totalPriceTextView.text.toString().replace("₱", "").toDouble()

            if (deliveryMethod == "Delivery" && deliveryAddressInput.text.toString().isEmpty()) {
                showErrorDialog("Please enter delivery address")
                payNowButton.isEnabled = true
                return@setOnClickListener
            }

            when (paymentMethodGroup.checkedRadioButtonId) {
                R.id.cashRadio -> {
                    val cashAmount = cashAmountInput.text.toString().toDoubleOrNull() ?: 0.0

                    if (cashAmount < total) {
                        showErrorDialog("Cash amount must be greater than total")
                        payNowButton.isEnabled = true
                        return@setOnClickListener
                    }

                    val change = cashAmount - total
                    val paymentDetails =
                        "Payment Method: Cash\nAmount Given: ₱${"%.2f".format(cashAmount)}\nChange: ₱${
                            "%.2f".format(change)
                        }"

                    processPayment(
                        deliveryMethod = deliveryMethod,
                        paymentMethod = "Cash",
                        referenceNumber = "₱${"%.2f".format(cashAmount)} change ₱${"%.2f".format(change)}",
                        paymentDetails = paymentDetails,
                        totalAmount = total,
                        deliveryAddress = if (deliveryMethod == "Delivery") deliveryAddressInput.text.toString() else ""
                    )
                }

                R.id.gcashRadio -> {
                    val refNumber = gcashRefInput.text.toString()

                    if (refNumber.isEmpty()) {
                        showErrorDialog("Please enter GCash reference number")
                        payNowButton.isEnabled = true
                        return@setOnClickListener
                    }

                    val paymentDetails = "Payment Method: GCash\nReference Number: $refNumber"

                    processPayment(
                        deliveryMethod = deliveryMethod,
                        paymentMethod = "GCash",
                        referenceNumber = refNumber,
                        paymentDetails = paymentDetails,
                        totalAmount = total,
                        deliveryAddress = if (deliveryMethod == "Delivery") deliveryAddressInput.text.toString() else ""
                    )
                }

                else -> {
                    showErrorDialog("Please select a payment method")
                    payNowButton.isEnabled = true
                }
            }
        }
    }

    private fun processPayment(
        deliveryMethod: String,
        paymentMethod: String,
        referenceNumber: String,
        paymentDetails: String,
        totalAmount: Double,
        deliveryAddress: String
    ) {
        val userId = MyApplication.studentId!!
        val userName = MyApplication.name!!
        val batchId = System.currentTimeMillis().toString()

        val orderBatch = OrderBatch(
            batchId = batchId,
            userId = userId,
            userName = userName,
            orders = cartOrders,
            totalAmount = totalAmount,
            status = "Pending",
            paymentMethod = paymentMethod,
            referenceNumber = referenceNumber,
            deliveryType = deliveryMethod,
            deliveredBy = "",
            deliveryAddress = deliveryAddress,
            deliveryFee = calculateDeliveryFee(),
            timestamp = System.currentTimeMillis()
        )

        FirebaseRepository().addOrderBatch(
            orderBatch = orderBatch,
            onSuccess = {
                deleteAllOrdersFromCart(userId, onSuccess = {
                    MyApplication.orders.clear()
                    MyApplication.clearOrdersCache()

                    showOrderConfirmationDialog(
                        deliveryMethod = deliveryMethod,
                        paymentDetails = paymentDetails,
                        totalAmount = "₱${"%.2f".format(totalAmount)}",
                        deliveryAddress = deliveryAddress
                    )
                }, onFailure = { e ->
                    showErrorDialog("Order placed but failed to clear cart: ${e.message}")
                    payNowButton.isEnabled = true
                })
            },
            onFailure = { e ->
                showErrorDialog("Failed to place order: ${e.message}")
                payNowButton.isEnabled = true
            }
        )
    }

    private fun calculateDeliveryFee(): Double {
        val subtotal = cartOrders.sumOf { it.items.price * it.quantity }
        return if (deliveryMethodGroup.checkedRadioButtonId == R.id.deliveryRadio) {
            if (subtotal >= 100) subtotal * 0.10 else 10.0
        } else {
            0.0
        }
    }

    private fun deleteAllOrdersFromCart(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseRepository().db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = FirebaseRepository().db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    private fun showOrderConfirmationDialog(
        deliveryMethod: String,
        paymentDetails: String,
        totalAmount: String,
        deliveryAddress: String
    ) {
        val message = buildString {
            append("ORDER CONFIRMED\n")
            append("===============\n")
            append("Delivery: $deliveryMethod\n\n")

            if (deliveryMethod == "Delivery") {
                append("Delivery Address: $deliveryAddress\n\n")
            }

            append("$paymentDetails\n\n")
            append("Total Amount: $totalAmount\n\n")
            append("Thank you for your order!\n")
            append("Your food will be ready soon.")
        }

        AlertDialog.Builder(this)
            .setTitle("Order Confirmation")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                payNowButton.isEnabled = true
            }
            .setOnDismissListener {
                payNowButton.isEnabled = true
            }
            .show()
    }

    private fun populateOrderItems() {
        orderItemsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        cartOrders.forEach { item ->
            val itemView = inflater.inflate(R.layout.row_order_summary, orderItemsContainer, false)
            val itemName = itemView.findViewById<TextView>(R.id.itemName)
            val itemTotalAmount = itemView.findViewById<TextView>(R.id.itemTotalAmount)

            itemName.text = "${item.quantity}x ${item.items.name}"
            itemTotalAmount.text = "₱${"%.2f".format(item.items.price * item.quantity)}"
            orderItemsContainer.addView(itemView)
        }
    }

    private fun updateTotalPrice() {
        val subtotal = cartOrders.sumOf { it.items.price * it.quantity }
        val deliveryFee = calculateDeliveryFee()
        val discount = 5.00

        deliveryFeeTextView.text = "₱${"%.2f".format(deliveryFee)}"
        discountTextView.text = "-₱${"%.2f".format(discount)}"
        val total = subtotal - discount + deliveryFee
        totalPriceTextView.text = "₱${"%.2f".format(total)}"
    }

    private fun updateDeliveryFee(checkedId: Int) {
        updateTotalPrice()
    }
}