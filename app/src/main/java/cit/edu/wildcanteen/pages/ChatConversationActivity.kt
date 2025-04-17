package cit.edu.wildcanteen.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.MessageAdapter
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.databinding.ActivityChatConversationBinding
import cit.edu.wildcanteen.repositories.FoodRepository
import java.util.*

class ChatConversationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatConversationBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var currentUserId: String = "user2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val senderId = intent.getStringExtra("senderId") ?: ""

        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        loadConversationMessages()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Chat"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(currentUserId)
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatConversationActivity).apply {
                stackFromEnd = true // Ensure it starts from the bottom
            }
            adapter = messageAdapter
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    post { scrollToPosition(messageAdapter.itemCount - 1) }
                }
            }
        }
    }

    private fun loadConversationMessages() {
        val senderId = intent.getStringExtra("senderId") ?: ""

        val conversationMessages = FoodRepository.loadSampleData().filter {
            (it.senderId == currentUserId && it.recipientId == senderId) ||
                    (it.senderId == senderId && it.recipientId == currentUserId)
        }.sortedBy { it.timestamp }

        messages.clear()
        messages.addAll(conversationMessages)

        messageAdapter.submitList(messages.toList())
        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
    }


    private fun setupMessageInput() {
        val inputShape = resources.getDrawable(R.drawable.rounded_drawable)
        binding.messageInput.background = inputShape

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun sendMessage(text: String) {
        val newMessage = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            senderId = currentUserId,
            senderName = "You",
            senderImage = "",
            recipientId = intent.getStringExtra("senderId") ?: "",
            messageText = text,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        messages.add(newMessage)
        messageAdapter.submitList(messages.toList())
        binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
    }
}
