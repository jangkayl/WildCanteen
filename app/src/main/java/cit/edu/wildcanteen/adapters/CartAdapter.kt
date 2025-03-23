package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R

class CartAdapter(private val cartOrders: MutableList<Order>, private val onRemove: (Order) -> Unit) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val removeButton: ImageView = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val order = cartOrders[position]
        val firstItem = order.items

        if (firstItem != null) {
            holder.foodImage.setImageResource(firstItem.imageResId)
            holder.foodName.text = firstItem.name
            holder.foodPrice.text = "₱${firstItem.price}"
        } else {
            holder.foodName.text = "Unknown Item"
            holder.foodPrice.text = "₱0.00"
        }

        holder.removeButton.setOnClickListener {
            onRemove(order)
        }
    }

    override fun getItemCount() = cartOrders.size
}
