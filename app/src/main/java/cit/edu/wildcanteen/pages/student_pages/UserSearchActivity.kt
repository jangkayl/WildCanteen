package cit.edu.wildcanteen.pages.student_pages

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cit.edu.wildcanteen.UserInfo
import cit.edu.wildcanteen.adapters.UserAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.databinding.ActivityUserSearchBinding
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class UserSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserSearchBinding
    private lateinit var userAdapter: UserAdapter
    private val allUsers = mutableListOf<UserInfo>()
    private lateinit var firebaseRepository: FirebaseRepository
    private var usersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "New Message"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseRepository = FirebaseRepository()
        setupRecyclerView()
        setupSearchListener()
        loadAllUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            startChatWithUser(user)
        }

        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserSearchActivity)
            adapter = userAdapter
        }
    }

    private fun setupSearchListener() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }
        })
    }

    private fun loadAllUsers() {
        val currentUserId = MyApplication.studentId ?: return

        usersListener = firebaseRepository.getAllUsers(
            onSuccess = { users ->
                allUsers.clear()
                // Filter out current user
                allUsers.addAll(users.filter { it.userId != currentUserId })
                userAdapter.submitList(allUsers)
            },
            onFailure = { exception ->
                Log.e("UserSearch", "Error loading users", exception)
            }
        )
    }

    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name?.contains(query, ignoreCase = true) == true ||
                        user.userId?.contains(query, ignoreCase = true) == true
            }
        }
        userAdapter.submitList(filteredList)
    }

    private fun startChatWithUser(user: UserInfo) {
        val intent = Intent(this, ChatConversationActivity::class.java).apply {
            putExtra("senderId", MyApplication.studentId)
            putExtra("senderName", MyApplication.name)
            putExtra("senderImage", MyApplication.profileImageUrl)
            putExtra("recipientId", user.userId)
            putExtra("recipientName", user.name)
            putExtra("recipientImage", user.profileImageUrl)
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        usersListener?.remove()
    }
}