package cit.edu.wildcanteen.pages

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import java.text.SimpleDateFormat
import java.util.*

class OrderBatchAdapter(
    private val onItemClick: (OrderBatch) -> Unit
) : ListAdapter<OrderBatch, OrderBatchAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val batchId: TextView = view.findViewById(R.id.batchIdText)
        val customerName: TextView = view.findViewById(R.id.customerNameText)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountText)
        val status: TextView = view.findViewById(R.id.statusText)
        val date: TextView = view.findViewById(R.id.dateText)
        val itemCount: TextView = view.findViewById(R.id.itemCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_batch, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batch = getItem(position)

        holder.batchId.text = "Order #${batch.batchId.takeLast(6)}"
        holder.customerName.text = batch.userName
        holder.totalAmount.text = "₱${"%.2f".format(batch.totalAmount)}"
        holder.status.text = batch.status
        holder.date.text = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            .format(Date(batch.timestamp))
        holder.itemCount.text = "${batch.orders.size} items"

        // Set status color
        when (batch.status.lowercase()) {
            "pending" -> holder.status.setTextColor(Color.parseColor("#FFA500")) // Orange
            "preparing" -> holder.status.setTextColor(Color.parseColor("#2196F3")) // Blue
            "ready" -> holder.status.setTextColor(Color.parseColor("#4CAF50")) // Green
            "completed" -> holder.status.setTextColor(Color.parseColor("#607D8B")) // Gray
            "cancelled" -> holder.status.setTextColor(Color.parseColor("#F44336")) // Red
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