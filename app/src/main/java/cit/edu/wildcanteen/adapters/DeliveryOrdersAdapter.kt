package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.OrderBatch
import java.text.SimpleDateFormat
import java.util.*

class DeliveryOrdersAdapter(
    private val onItemClick: (OrderBatch) -> Unit
) : ListAdapter<OrderBatch, DeliveryOrdersAdapter.DeliveryOrderViewHolder>(DiffCallback()) {

    class DeliveryOrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerName: TextView = view.findViewById(R.id.customerName)
        val orderTime: TextView = view.findViewById(R.id.orderTime)
        val totalAmount: TextView = view.findViewById(R.id.totalAmount)
        val address: TextView = view.findViewById(R.id.deliveryAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_order, parent, false)
        return DeliveryOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryOrderViewHolder, position: Int) {
        val order = getItem(position)

        holder.customerName.text = order.userName
        holder.totalAmount.text = "₱${"%.2f".format(order.totalAmount)}"

        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        holder.orderTime.text = sdf.format(Date(order.timestamp))

        // You'll need to add address field to your OrderBatch model
        // holder.address.text = order.deliveryAddress

        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    class DiffCallback : DiffUtil.ItemCallback<OrderBatch>() {
        override fun areItemsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem.batchId == newItem.batchId

        override fun areContentsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem == newItem
    }
}