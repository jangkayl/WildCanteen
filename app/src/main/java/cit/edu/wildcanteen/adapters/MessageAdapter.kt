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
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val currentUserId: String) :
    ListAdapter<ChatMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val ONE_MINUTE = 60000L
        private const val ONE_HOUR = 3600000L
        private const val ONE_DAY = 86400000L
        private const val ONE_WEEK = ONE_DAY * 7
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
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Abbreviated day name (Mon, Tue, etc.)
            val dateTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

            return when {
                diff < ONE_MINUTE -> "Just now"
                diff < ONE_HOUR -> "${diff / ONE_MINUTE}m ago"
                isToday(calendar) -> timeFormat.format(Date(timestamp))
                isYesterday(calendar) -> "Yesterday, ${timeFormat.format(Date(timestamp))}"
                isWithinWeek(diff) -> "${dayOfWeekFormat.format(Date(timestamp))}, ${timeFormat.format(Date(timestamp))}"
                else -> dateTimeFormat.format(Date(timestamp))
            }
        }

        private fun isToday(calendar: Calendar): Boolean {
            val today = Calendar.getInstance()
            return (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        }

        private fun isYesterday(calendar: Calendar): Boolean {
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            return (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR))
        }

        private fun isWithinWeek(diff: Long): Boolean {
            return diff < ONE_WEEK
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