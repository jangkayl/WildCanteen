package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val onItemClick: (ChatMessage) -> Unit) : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private var searchQuery: String = ""

    companion object {
        private const val ONE_MINUTE = 60000L
        private const val ONE_HOUR = 3600000L
        private const val ONE_DAY = 86400000L
        private const val ONE_WEEK = ONE_DAY * 7
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        notifyDataSetChanged()
    }

    fun forcefullyUpdateList(newList: List<ChatMessage>) {
        submitList(null)
        submitList(newList.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position), searchQuery)
    }

    class ChatViewHolder(itemView: View, private val onItemClick: (ChatMessage) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val avatarImage: ImageView = itemView.findViewById(R.id.user_avatar)
        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.message_time)
        private val unreadBadge: View = itemView.findViewById(R.id.unread_badge)

        fun bind(chatMessage: ChatMessage, searchQuery: String) {
            val isCurrentUserSender = chatMessage.senderId == MyApplication.studentId
            val displayName = if (isCurrentUserSender) chatMessage.recipientName else chatMessage.senderName
            val displayImage = if (isCurrentUserSender) chatMessage.recipientImage else chatMessage.senderImage

            userName.text = highlightText(displayName, searchQuery)
            messageText.text = highlightText(chatMessage.messageText, searchQuery)
            messageTime.text = formatTimestamp(chatMessage.timestamp)

            if (!isCurrentUserSender && !chatMessage.isRead) {
                unreadBadge.isVisible = true
                messageText.setTypeface(null, android.graphics.Typeface.BOLD)
                messageText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            } else {
                unreadBadge.isVisible = false
                messageText.setTypeface(null, android.graphics.Typeface.NORMAL)
                messageText.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
            }

            Glide.with(itemView.context)
                .load(displayImage)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .into(avatarImage)

            itemView.setOnClickListener {
                onItemClick(chatMessage)
            }
        }

        private fun highlightText(text: String, query: String): CharSequence {
            return if (query.isEmpty()) {
                text
            } else {
                val spannable = android.text.SpannableString(text)
                val lowerText = text.lowercase(Locale.getDefault())
                val lowerQuery = query.lowercase(Locale.getDefault())

                var start = 0
                while (true) {
                    start = lowerText.indexOf(lowerQuery, start)
                    if (start == -1) break

                    val end = start + query.length
                    val highlightColor = ContextCompat.getColor(itemView.context, R.color.selectedColor)
                    spannable.setSpan(
                        android.text.style.ForegroundColorSpan(highlightColor),
                        start, end,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    start = end
                }
                spannable
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

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
