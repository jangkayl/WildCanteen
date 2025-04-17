package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.R
import com.bumptech.glide.Glide
import androidx.cardview.widget.CardView

class MessageAdapter(private val currentUserId: String) :
    ListAdapter<ChatMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = when (viewType) {
            VIEW_TYPE_SENT -> R.layout.item_message_sent
            else -> R.layout.item_message_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view, viewType == VIEW_TYPE_RECEIVED)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(itemView: View, private val isReceived: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)
        private val messageCard: CardView = itemView.findViewById(R.id.messageCard)
        private val userAvatar: ImageView? = itemView.findViewById(R.id.userAvatar)

        fun bind(message: ChatMessage) {
            messageText.text = message.messageText
            messageTime.text = formatTimestamp(message.timestamp)

            if (isReceived) {
                messageCard.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
                messageText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))

                userAvatar?.let {
                    Glide.with(itemView.context)
                        .load(message.senderImage)
                        .placeholder(R.drawable.hd_user)
                        .error(R.drawable.hd_user)
                        .circleCrop()
                        .into(it)
                }
            } else {
                messageCard.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.selectedColor)
                )
                messageText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                else -> android.text.format.DateFormat.format("h:mm a", timestamp).toString()
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
