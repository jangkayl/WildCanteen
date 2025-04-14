package cit.edu.wildcanteen.pages.admin_page

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import com.bumptech.glide.Glide

class AllOrderAdapter(
    private val context: Context,
    private val orders: List<Order>,
    private val onChangeStatusClick: (Order) -> Unit
) : RecyclerView.Adapter<AllOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvTotalAmount: TextView = view.findViewById(R.id.tvTotalAmount)
        val userFullName: TextView = view.findViewById(R.id.userFullName)
        val userId: TextView = view.findViewById(R.id.userId)
        val paymentMethod: TextView = view.findViewById(R.id.paymentMethod)
        val deliveryMethod: TextView = view.findViewById(R.id.deliveryMethod)
        val btnChangeStatus: Button = view.findViewById(R.id.btnChangeStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.foodName.text = order.items.name
        holder.foodPrice.text = "₱%.2f".format(order.items.price)
        holder.tvQuantity.text = "${order.quantity}"
        holder.tvTotalAmount.text = "Total: ₱%.2f".format(order.items.price * order.quantity)
        holder.userFullName.text = order.userName
        holder.userId.text = order.userId
//        holder.paymentMethod.text = "Payment: ${order.paymentMethod}"
//        holder.deliveryMethod.text = "Delivery: ${order.deliveryMethod}"

        Glide.with(context)
            .load(order.items.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.foodImage)

        holder.btnChangeStatus.setOnClickListener {
            onChangeStatusClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size
}
