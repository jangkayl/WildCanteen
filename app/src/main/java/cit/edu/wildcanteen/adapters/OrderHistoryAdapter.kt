package cit.edu.wildcanteen.adapters

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

class OrderHistoryAdapter : ListAdapter<OrderBatch, OrderHistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val batchId: TextView = view.findViewById(R.id.batchIdText)
        val status: TextView = view.findViewById(R.id.statusText)
        val date: TextView = view.findViewById(R.id.dateText)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batch = getItem(position)

        holder.batchId.text = "Order #${batch.batchId.takeLast(6)}"
        holder.status.text = batch.status
        holder.totalAmount.text = "₱${"%.2f".format(batch.totalAmount)}"
        holder.date.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(batch.timestamp))

        // Set status color
        when (batch.status.lowercase()) {
            "completed" -> holder.status.setTextColor(Color.parseColor("#4CAF50")) // Green
            "cancelled" -> holder.status.setTextColor(Color.parseColor("#F44336")) // Red
            else -> holder.status.setTextColor(Color.parseColor("#607D8B")) // Gray
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<OrderBatch>() {
        override fun areItemsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem.batchId == newItem.batchId

        override fun areContentsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem == newItem
    }
}