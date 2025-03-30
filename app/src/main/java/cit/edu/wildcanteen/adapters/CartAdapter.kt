package cit.edu.wildcanteen.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.FoodDetailsActivity
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import com.bumptech.glide.Glide

class CartAdapter(
    private val context: Context,
    private val cartOrders: MutableList<Order>,
    private val onRemove: (Order) -> Unit,
    private val onUpdateTotalAmount: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val removeButton: ImageView = view.findViewById(R.id.removeButton)
        val increaseButton: Button = view.findViewById(R.id.btnPlus)
        val reduceButton: Button = view.findViewById(R.id.btnMinus)
        val quantity: TextView = view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val order = cartOrders[position]
        val firstItem = order.items

        val storedOrder = MyApplication.orders.find { it.orderId == order.orderId }
        order.quantity = storedOrder?.quantity ?: order.quantity

        holder.quantity.text = order.quantity.toString()

        if (firstItem != null) {
            Glide.with(holder.itemView.context)
                .load(firstItem.imageUrl)
                .into(holder.foodImage)
        } else {
            holder.foodImage.setImageResource(R.drawable.chicken)
        }

        holder.foodName.text = firstItem?.name
        holder.foodPrice.text = "₱%.2f".format(firstItem?.price)

        holder.removeButton.setOnClickListener {
            onRemove(order)
            MyApplication.orders.remove(order)
            val ordersToRemove = listOf(order)
            MyApplication.saveOrders(ordersToRemove)
            onUpdateTotalAmount()
        }

        holder.itemView.setOnClickListener { view ->
            if (firstItem != null && view.id != R.id.cart_buttons) {
                val intent = Intent(context, FoodDetailsActivity::class.java).apply {
                    putExtra("FOOD_CATEGORY", firstItem.category)
                    putExtra("FOOD_NAME", firstItem.name)
                    putExtra("FOOD_PRICE", firstItem.price.toString())
                    putExtra("FOOD_RATING", firstItem.rating.toString())
                    putExtra("FOOD_DESCRIPTION", firstItem.description)
                    putExtra("FOOD_IMAGE", firstItem.imageUrl)
                    putExtra("FOOD_POPULAR", firstItem.isPopular)
                }
                context.startActivity(intent)
            }
        }

        holder.increaseButton.setOnClickListener {
            order.quantity++
            order.totalAmount = order.quantity * order.items.price
            holder.quantity.text = order.quantity.toString()

            MyApplication.saveOrders(emptyList())
            onUpdateTotalAmount()
            notifyItemChanged(position)
            Log.d("CartAdapter", "Increased: ${order.items.name} - New Quantity: ${order.quantity}")
        }

        holder.reduceButton.setOnClickListener {
            if (order.quantity > 1) {
                order.quantity--
                order.totalAmount = order.quantity * order.items.price
                holder.quantity.text = order.quantity.toString()

                MyApplication.saveOrders(emptyList())
                onUpdateTotalAmount()
                notifyItemChanged(position)
                Log.d("CartAdapter", "Decreased: ${order.items.name} - New Quantity: ${order.quantity}")
            }
        }
    }

    override fun getItemCount() = cartOrders.size
}