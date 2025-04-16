package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R

class OrderItemAdapter : ListAdapter<Order, OrderItemAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemNameText)
        val itemPrice: TextView = view.findViewById(R.id.itemPriceText)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantityText)
        val itemTotal: TextView = view.findViewById(R.id.itemTotalText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = getItem(position)

        holder.itemName.text = order.items.name
        holder.itemPrice.text = "₱${"%.2f".format(order.items.price)}"
        holder.itemQuantity.text = "x${order.quantity}"
        holder.itemTotal.text = "₱${"%.2f".format(order.items.price * order.quantity)}"
    }

    private class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) =
            oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(oldItem: Order, newItem: Order) =
            oldItem == newItem
    }
}