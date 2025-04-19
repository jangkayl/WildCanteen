package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DeliveryOrderAdapter(
    private val onItemClick: (OrderBatch) -> Unit,
    private val onAcceptClick: (OrderBatch) -> Unit
) : ListAdapter<OrderBatch, DeliveryOrderAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val batchId: TextView = view.findViewById(R.id.batchIdText)
        val customerName: TextView = view.findViewById(R.id.customerNameText)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountText)
        val deliveryFee: TextView = view.findViewById(R.id.deliveryFeeText)
        val date: TextView = view.findViewById(R.id.dateText)
        val itemCount: TextView = view.findViewById(R.id.itemCountText)
        val address: TextView = view.findViewById(R.id.addressText)
        val paymentMethod: TextView = view.findViewById(R.id.paymentMethodText)
        val changeAmount: TextView = view.findViewById(R.id.changeAmountText)
        val acceptBtn: Button = view.findViewById(R.id.acceptDeliveryBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batch = getItem(position)

        holder.batchId.text = "Order #${batch.batchId.takeLast(6)}"
        holder.customerName.text = batch.userName
        holder.totalAmount.text = "₱${"%.2f".format(batch.totalAmount)}"
        holder.deliveryFee.text = "Earn: ₱${"%.2f".format(batch.deliveryFee)}"
        holder.date.text = getRelativeTime(batch.timestamp)
        holder.itemCount.text = "${batch.orders.size} items"
        holder.address.text = batch.deliveryAddress

        if (batch.paymentMethod.equals("Cash", ignoreCase = true)) {
            holder.paymentMethod.text = "Cash on Delivery"
            holder.paymentMethod.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.selectedColor))
            holder.changeAmount.text = batch.referenceNumber
            holder.changeAmount.visibility = View.VISIBLE
        } else {
            holder.paymentMethod.text = "Paid Online"
            holder.paymentMethod.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            holder.changeAmount.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(batch) }
        holder.acceptBtn.setOnClickListener { onAcceptClick(batch) }

        if(batch.deliveredBy != ""){
            holder.acceptBtn.text = "Chat Buyer"
            holder.acceptBtn.backgroundTintList = ContextCompat.getColorStateList(
                holder.itemView.context,
                R.color.green
            )
        }

        if(batch.deliveredBy != "" && batch.status.equals("Completed", ignoreCase = true)){
            holder.deliveryFee.text = "Earn: ₱${"%.2f".format(batch.deliveryFee)}"
            holder.acceptBtn.visibility = View.GONE
            holder.deliveryFee.text = "Earned: ₱${"%.2f".format(batch.deliveryFee)}"
            holder.deliveryFee.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
    }

    private fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes min${if (minutes > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            else -> SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<OrderBatch>() {
        override fun areItemsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem.batchId == newItem.batchId

        override fun areContentsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem == newItem
    }
}