package cit.edu.wildcanteen.pages

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.MessageAdapter
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.databinding.ActivityChatConversationBinding
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class ChatConversationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatConversationBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var firebaseRepository: FirebaseRepository
    private var messagesListener: ListenerRegistration? = null
    private lateinit var currentUserId: String
    private lateinit var otherUserId: String
    private lateinit var otherUserName: String
    private lateinit var otherUserImage: String
    private lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseRepository = FirebaseRepository()

        val chatUserImage = findViewById<ImageView>(R.id.chatUserImage)
        currentUserId = MyApplication.studentId!!
        if(intent.getStringExtra("senderId") == currentUserId){
            otherUserId = intent.getStringExtra("recipientId")!!
            otherUserName = intent.getStringExtra("recipientName")!!
            otherUserImage = intent.getStringExtra("recipientImage")!!
        } else {
            otherUserId = intent.getStringExtra("senderId")!!
            otherUserName = intent.getStringExtra("senderName")!!
            otherUserImage = intent.getStringExtra("senderImage")!!
        }

        roomId = listOf(currentUserId, otherUserId).sorted().joinToString("_")

        Glide.with(this)
            .load(otherUserImage)
            .placeholder(R.drawable.hd_user)
            .error(R.drawable.hd_user)
            .circleCrop()
            .into(chatUserImage)

        findViewById<TextView>(R.id.chatUserTitle).text = otherUserName
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupMessageInput()
        setupMessagesListener()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(currentUserId)
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatConversationActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    post { scrollToPosition(messageAdapter.itemCount - 1) }
                }
            }
        }
    }

    private fun setupMessagesListener() {
        messagesListener = firebaseRepository.listenForConversationMessages(
            currentUserId,
            otherUserId,
            onUpdate = { newMessages ->
                messages.clear()
                messages.addAll(newMessages.sortedBy { it.timestamp })
                messageAdapter.submitList(messages.toList())

                // Scroll to bottom when new messages arrive
                binding.messagesRecyclerView.post {
                    binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                }

                // Mark received messages as read
                newMessages.filter {
                    it.recipientId == currentUserId && !it.isRead
                }.forEach { message ->
                    firebaseRepository.markMessageAsRead(message.messageId)
                }
            },
            onFailure = { exception ->
                Log.e("ChatActivity", "Error listening for messages", exception)
            }
        )
    }

    private fun setupMessageInput() {
        val inputShape = resources.getDrawable(R.drawable.rounded_drawable)
        binding.messageInput.background = inputShape

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun sendMessage(text: String) {
        val newMessage = ChatMessage(
            messageId = "",
            roomId = roomId,
            senderId = currentUserId,
            senderName = MyApplication.name ?: "You",
            senderImage = MyApplication.profileImageUrl ?: "",
            recipientId = otherUserId,
            recipientName = otherUserName,
            recipientImage = otherUserImage,
            messageText = text,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        firebaseRepository.sendChatMessage(
            newMessage,
            onSuccess = {
                Log.d("Chat", "Message sent successfully")
            },
            onFailure = { exception ->
                Log.e("Chat", "Failed to send message", exception)
                binding.messageInput.setText(text)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }
}