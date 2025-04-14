package cit.edu.wildcanteen.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import java.text.SimpleDateFormat
import java.util.*

class OrderedBatchesAdapter(
    private val onItemClick: (OrderBatch) -> Unit
) : ListAdapter<OrderBatch, OrderedBatchesAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderNumber: TextView = view.findViewById(R.id.orderNumberText)
        val status: TextView = view.findViewById(R.id.statusText)
        val date: TextView = view.findViewById(R.id.dateText)
        val itemsContainer: LinearLayout = view.findViewById(R.id.itemsContainer)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batch = getItem(position)

        // Set basic info
        holder.orderNumber.text = "Order #${batch.batchId.takeLast(6)}"
        holder.status.text = batch.status.uppercase()
        holder.date.text = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            .format(Date(batch.timestamp))
        holder.totalAmount.text = "₱${"%.2f".format(batch.totalAmount)}"

        // Set status color
        when (batch.status.lowercase()) {
            "pending" -> holder.status.setTextColor(Color.parseColor("#FFA500")) // Orange
            "preparing" -> holder.status.setTextColor(Color.parseColor("#2196F3")) // Blue
            "ready" -> holder.status.setTextColor(Color.parseColor("#4CAF50")) // Green
            else -> holder.status.setTextColor(Color.parseColor("#607D8B")) // Gray
        }

        // Clear previous items
        holder.itemsContainer.removeAllViews()

        // Add each item to the container
        batch.orders.forEach { order ->
            val itemView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_order_line, holder.itemsContainer, false)

            val name = itemView.findViewById<TextView>(R.id.itemName)
            val quantity = itemView.findViewById<TextView>(R.id.itemQuantity)
            val price = itemView.findViewById<TextView>(R.id.itemPrice)

            name.text = order.items.name
            quantity.text = "x${order.quantity}"
            price.text = "₱${"%.2f".format(order.items.price * order.quantity)}"

            holder.itemsContainer.addView(itemView)
        }

        holder.itemView.setOnClickListener {
            onItemClick(batch)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<OrderBatch>() {
        override fun areItemsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem.batchId == newItem.batchId

        override fun areContentsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem == newItem
    }
}