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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val onItemClick: (ChatMessage) -> Unit) : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private var searchQuery: String = ""

    fun setSearchQuery(query: String) {
        searchQuery = query
        notifyDataSetChanged()
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
            userName.text = highlightText(chatMessage.senderName, searchQuery)
            messageText.text = highlightText(chatMessage.messageText, searchQuery)
            messageTime.text = formatTimestamp(chatMessage.timestamp)
            unreadBadge.isVisible = !chatMessage.isRead

            Glide.with(itemView.context)
                .load(chatMessage.senderImage)
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

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
                diff < 604800000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
            }
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
