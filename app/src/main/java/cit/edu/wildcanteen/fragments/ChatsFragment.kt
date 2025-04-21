package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.ChatAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.ChatConversationActivity
import cit.edu.wildcanteen.pages.UserSearchActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository

class ChatsFragment : Fragment() {
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var searchEditText: EditText
    private lateinit var fabNewMessage: FloatingActionButton
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var noChatLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chatpage_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.chats_recycler_view)
        searchEditText = view.findViewById(R.id.search_chats)
        fabNewMessage = view.findViewById(R.id.fab_new_message)
        firebaseRepository = FirebaseRepository()
        noChatLayout = view.findViewById(R.id.no_chat_layout)

        setupRecyclerView()
        setupSearchFunctionality()
        setupFabClickListener()
        setupChatObserver()

        updateChatList()

        // Add this to print all chat details when fragment is opened
        Log.d("ChatsFragment", "Current user ID: ${MyApplication.studentId}")
        Log.d("ChatsFragment", "All chats count: ${MyApplication.allChats.size}")
        MyApplication.allChats.forEachIndexed { index, chat ->
            Log.d("ChatsFragment", """
            Chat $index:
            - Message ID: ${chat.messageId}
            - Sender: ${chat.senderName} (${chat.senderId})
            - Recipient: ${chat.recipientName} (${chat.recipientId})
            - Message: ${chat.messageText}
            - Timestamp: ${chat.timestamp}
            - Read: ${chat.isRead}
        """.trimIndent())
        }
    }

    override fun onResume() {
        super.onResume()
        updateChatList()
        Log.d("ChatsFragment", "onResume: forcing UI update")
    }

    private fun setupChatObserver() {
        MyApplication.chatUpdatesLiveData.removeObservers(viewLifecycleOwner)

        MyApplication.chatUpdatesLiveData.observe(viewLifecycleOwner, Observer { chatMessages ->
            Log.d("ChatsFragment", "Observer triggered with ${chatMessages.size} messages")
            handler.post {
                Log.d("ChatsFragment", "Handler executing UI update on main thread")
                updateChatList()
                recyclerView.invalidate()
            }
        })
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatAdapter { chatMessage ->
            if (!chatMessage.isRead && chatMessage.recipientId == MyApplication.studentId) {
                firebaseRepository.markMessageAsRead(chatMessage.messageId)
            }

            val intent = Intent(context, ChatConversationActivity::class.java).apply {
                putExtra("senderId", chatMessage.senderId)
                putExtra("senderName", chatMessage.senderName)
                putExtra("senderImage", chatMessage.senderImage)
                putExtra("recipientId", chatMessage.recipientId)
                putExtra("recipientName", chatMessage.recipientName)
                putExtra("recipientImage", chatMessage.recipientImage)
            }
            context?.startActivity(intent)
        }
        recyclerView.adapter = chatAdapter
    }

    private fun updateChatList() {
        if (!isAdded) return

        val currentQuery = searchEditText.text?.toString()?.trim() ?: ""
        val filteredChats = if (currentQuery.isNotEmpty()) {
            MyApplication.allChats.filter { chat ->
                chat.senderName.contains(currentQuery, ignoreCase = true) ||
                        chat.recipientName.contains(currentQuery, ignoreCase = true) ||
                        chat.messageText.contains(currentQuery, ignoreCase = true)
            }
        } else {
            MyApplication.allChats
        }

        noChatLayout.visibility = if (filteredChats.isEmpty()) View.VISIBLE else View.GONE
        chatAdapter.submitList(filteredChats)
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterChats(s?.toString() ?: "")
            }
        })
    }

    private fun filterChats(query: String) {
        val filteredList = if (query.isEmpty()) {
            MyApplication.allChats
        } else {
            MyApplication.allChats.filter { chat ->
                val otherPersonName = if (chat.senderId == MyApplication.studentId) chat.recipientName else chat.senderName
                otherPersonName.contains(query, ignoreCase = true) ||
                        chat.messageText.contains(query, ignoreCase = true)
            }
        }
        chatAdapter.setSearchQuery(query)

        try {
            chatAdapter.forcefullyUpdateList(filteredList)
        } catch (e: Exception) {
            chatAdapter.submitList(filteredList)
        }
    }


    private fun setupFabClickListener() {
        fabNewMessage.setOnClickListener {
            val intent = Intent(requireContext(), UserSearchActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.chatUpdatesLiveData.removeObservers(viewLifecycleOwner)
    }
}