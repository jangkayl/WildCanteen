package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cit.edu.wildcanteen.ChatMessage
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.adapters.DeliveryOrderAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.databinding.FragmentAvailableOrdersBinding
import cit.edu.wildcanteen.pages.student_pages.ChatConversationActivity
import cit.edu.wildcanteen.pages.student_pages.OrderBatchDetailActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class AvailableOrdersFragment : Fragment() {
    private var _binding: FragmentAvailableOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DeliveryOrderAdapter
    private var orderBatchListener: ListenerRegistration? = null
    private var firebaseRepository = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailableOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadAvailableOrders()
    }

    private fun setupRecyclerView() {
        adapter = DeliveryOrderAdapter(
            onItemClick = { batch ->
                showOrderDetails(batch)
            },
            onAcceptClick = { batch ->
                acceptDelivery(batch)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun loadAvailableOrders() {
        binding.progressBar.visibility = View.VISIBLE
        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = null,
            onUpdate = { batches ->
                binding.progressBar.visibility = View.GONE
                val availableOrders = batches.filter { batch ->
                    batch.status == "Ready" &&
                            batch.deliveryType == "Delivery" &&
                            batch.userId != MyApplication.studentId
                }.sortedByDescending { it.timestamp }

                adapter.submitList(availableOrders)

                if (availableOrders.isEmpty()) {
                    binding.noOrderLayout.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.noOrderLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            },
            onFailure = { e ->
                binding.progressBar.visibility = View.GONE
                binding.noOrderLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
        )
    }

    private fun showOrderDetails(batch: OrderBatch) {
        val intent = Intent(requireContext(), OrderBatchDetailActivity::class.java).apply {
            putExtra("BATCH_ID", batch.batchId)
        }
        startActivity(intent)
    }

    private fun acceptDelivery(batch: OrderBatch) {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)

        builder.setTitle("Accept Delivery?")

        val message = """
        Do you want to accept this order?
        
        • Earn: ₱${"%.2f".format(batch.deliveryFee)}
        • Delivery Location: ${batch.deliveryAddress}
        • Orders in this batch: ${batch.orders.size}
    """.trimIndent()

        builder.setMessage(message)

        builder.setPositiveButton("Yes, Accept") { dialog, _ ->
            FirebaseRepository().acceptDeliveryOrder(batch.batchId, MyApplication.studentId!!, MyApplication.name!!) { success, e ->
                if (success) {
                   Toast.makeText(context, "You accepted the delivery!", Toast.LENGTH_SHORT).show()

                    sendMessage(batch, "Your Order #${batch.batchId.takeLast(6)} in now on the way!")

                } else {
                   Toast.makeText(context, "Failed to accept delivery.", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun sendMessage(batch: OrderBatch, text: String) {
        val roomId = listOf(MyApplication.studentId!!, batch.userId).sorted().joinToString("_")

        firebaseRepository.getUserProfileImageUrl(batch.userId, onSuccess = { recipientImageUrl ->
            val newMessage = ChatMessage(
                messageId = "",
                roomId = roomId,
                senderId = MyApplication.studentId!!,
                senderName = MyApplication.name ?: "You",
                senderImage = MyApplication.profileImageUrl ?: "",
                recipientId = batch.userId,
                recipientName = batch.userName,
                recipientImage = recipientImageUrl.toString(),
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
                }
            )

            val intent = Intent(context, ChatConversationActivity::class.java).apply {
                putExtra("senderId", MyApplication.studentId)
                putExtra("senderName", MyApplication.name)
                putExtra("senderImage", MyApplication.profileImageUrl)
                putExtra("recipientId", batch.userId)
                putExtra("recipientName", batch.userName)
                putExtra("recipientImage", recipientImageUrl)
            }
            context?.startActivity(intent)
        }, onFailure = {
            Toast.makeText(requireContext(), "Failed to load recipient image", Toast.LENGTH_SHORT).show()
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderBatchListener?.remove()
        _binding = null
    }
}