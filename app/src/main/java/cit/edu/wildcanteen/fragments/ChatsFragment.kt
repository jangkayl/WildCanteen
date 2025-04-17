package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.ChatAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.widget.EditText
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.pages.ChatConversationActivity
import cit.edu.wildcanteen.repositories.FoodRepository

class ChatsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var searchEditText: EditText
    private lateinit var fabNewMessage: FloatingActionButton
    private val allChats = mutableListOf<ChatMessage>()

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

        setupRecyclerView()
        loadSampleData()
        setupSearchFunctionality()
        setupFabClickListener()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatAdapter { chatMessage ->
            val intent = Intent(context, ChatConversationActivity::class.java)
            intent.putExtra("senderId", chatMessage.senderId)
            context?.startActivity(intent)
        }
        recyclerView.adapter = chatAdapter
    }


    private fun loadSampleData() {
        val sampleChats = FoodRepository.loadSampleData()

        allChats.clear()
        allChats.addAll(sampleChats)
        chatAdapter.submitList(allChats)
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterChats(s.toString())
            }
        })
    }

    private fun filterChats(query: String) {
        val filteredList = if (query.isEmpty()) {
            allChats
        } else {
            allChats.filter { chat ->
                chat.senderName.contains(query, ignoreCase = true) ||
                        chat.messageText.contains(query, ignoreCase = true)
            }
        }
        chatAdapter.setSearchQuery(query)
        chatAdapter.submitList(filteredList)
    }

    private fun setupFabClickListener() {
        fabNewMessage.setOnClickListener {
            // Handle new message button click
        }
    }
}