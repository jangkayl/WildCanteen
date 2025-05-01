package cit.edu.wildcanteen.adapters

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.student_pages.ChatConversationActivity
import cit.edu.wildcanteen.pages.student_pages.SendFeedbackActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import java.text.SimpleDateFormat
import java.util.*

class OrderedBatchesAdapter(
    private val onItemClick: (OrderBatch) -> Unit
) : ListAdapter<OrderBatch, OrderedBatchesAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val firebaseRepository = FirebaseRepository()
        val orderNumber: TextView = view.findViewById(R.id.orderNumberText)
        val status: TextView = view.findViewById(R.id.statusText)
        val date: TextView = view.findViewById(R.id.dateText)
        val itemsContainer: LinearLayout = view.findViewById(R.id.itemsContainer)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountText)
        val deliveringButton: LinearLayout = view.findViewById(R.id.deliveringButton)
        val messageDelivererButton: Button = view.findViewById(R.id.messageDelivererButton)
        val markAsDeliveredButton: Button = view.findViewById(R.id.markAsDelivered)
        val sendFeedback: TextView = view.findViewById(R.id.sendFeedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val batch = getItem(position)

        holder.orderNumber.text = "Order #${batch.batchId.takeLast(6)}"
        holder.status.text = batch.status.uppercase()
        holder.date.text = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            .format(Date(batch.timestamp))
        holder.totalAmount.text = "₱${"%.2f".format(batch.totalAmount)}"

        setStatusColor(holder, batch.status)

        populateOrderItems(holder, batch)

        holder.itemView.setOnClickListener {
            onItemClick(batch)
        }

        holder.deliveringButton.visibility = if (batch.status.equals("Delivering", ignoreCase = true)) View.VISIBLE else View.GONE
        holder.sendFeedback.visibility = if (batch.status.equals("Completed", ignoreCase = true)) View.VISIBLE else View.GONE

        holder.sendFeedback.setOnClickListener {
            val intent = Intent(holder.itemView.context, SendFeedbackActivity::class.java).apply {
                putExtra("BATCH_ID", batch.batchId)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.messageDelivererButton.setOnClickListener {
            handleMessageDelivererClick(holder, batch)
        }

        holder.markAsDeliveredButton.setOnClickListener {
            handleMarkAsDeliveredClick(holder, batch, position)
        }
    }

    private fun setStatusColor(holder: ViewHolder, status: String) {
        val color = when (status.lowercase()) {
            "preparing" -> "#2196F3" // Blue
            "delivering" -> "#4CAF50" // Green
            "ready" -> "#4CAF50" // Green
            "completed" -> "#4CAF50" // Green
            "cancelled" -> "#F44336" // Red
            else -> "#607D8B" // Grey
        }
        holder.status.setTextColor(Color.parseColor(color))
    }

    private fun populateOrderItems(holder: ViewHolder, batch: OrderBatch) {
        holder.itemsContainer.removeAllViews()

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
    }

    private fun handleMessageDelivererClick(holder: ViewHolder, batch: OrderBatch) {
        holder.firebaseRepository.getUserProfileImageUrl(batch.deliveredBy, onSuccess = { recipientImageUrl ->
            val intent = Intent(holder.itemView.context, ChatConversationActivity::class.java).apply {
                putExtra("senderId", MyApplication.studentId)
                putExtra("senderName", MyApplication.name)
                putExtra("senderImage", MyApplication.profileImageUrl)
                putExtra("recipientId", batch.deliveredBy)
                putExtra("recipientName", batch.deliveredByName)
                putExtra("recipientImage", recipientImageUrl)
            }
            holder.itemView.context.startActivity(intent)
        }, onFailure = {
            Toast.makeText(holder.itemView.context, "Failed to load recipient image", Toast.LENGTH_SHORT).show()
        })
    }

    private fun handleMarkAsDeliveredClick(holder: ViewHolder, batch: OrderBatch, position: Int) {
        val context = holder.itemView.context

        val dialog = android.app.AlertDialog.Builder(context)
            .setTitle("Confirm Delivery")
            .setMessage("Are you sure you have received your order?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseRepository().markOrderBatchAsCompleted(batch.batchId) { success, exception ->
                    if (success) {
                        Toast.makeText(context, "Marked as delivered!", Toast.LENGTH_SHORT).show()
                        sendMessage(batch, "Thank you for delivering the Order #${batch.batchId.takeLast(6)}. Your timely service is truly appreciated.", holder)
                        notifyItemChanged(position)
                    } else {
                        Toast.makeText(context, "Failed to update status: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun sendMessage(batch: OrderBatch, text: String, holder: ViewHolder){
        val roomId = listOf(MyApplication.studentId!!, batch.deliveredBy).sorted().joinToString("_")

        FirebaseRepository().getUserProfileImageUrl(batch.deliveredBy, onSuccess = { recipientImageUrl ->
            val newMessage = ChatMessage(
                messageId = "",
                roomId = roomId,
                senderId = MyApplication.studentId!!,
                senderName = MyApplication.name ?: "You",
                senderImage = MyApplication.profileImageUrl ?: "",
                recipientId = batch.deliveredBy,
                recipientName = batch.deliveredByName,
                recipientImage = recipientImageUrl.toString(),
                messageText = text,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            FirebaseRepository().sendChatMessage(
                newMessage,
                onSuccess = {
                    Log.d("Chat", "Message sent successfully")
                },
                onFailure = { exception ->
                    Log.e("Chat", "Failed to send message", exception)
                }
            )

            val intent = Intent(holder.itemView.context, ChatConversationActivity::class.java).apply {
                putExtra("senderId", MyApplication.studentId)
                putExtra("senderName", MyApplication.name)
                putExtra("senderImage", MyApplication.profileImageUrl)
                putExtra("recipientId", batch.deliveredBy)
                putExtra("recipientName", batch.deliveredByName)
                putExtra("recipientImage", recipientImageUrl)
            }
            holder.itemView.context.startActivity(intent)
        })
    }

    private class DiffCallback : DiffUtil.ItemCallback<OrderBatch>() {
        override fun areItemsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem.batchId == newItem.batchId

        override fun areContentsTheSame(oldItem: OrderBatch, newItem: OrderBatch) =
            oldItem == newItem
    }
}
