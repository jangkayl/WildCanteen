package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cit.edu.wildcanteen.OrderBatch
import cit.edu.wildcanteen.adapters.DeliveryOrderAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.databinding.FragmentAcceptedOrdersBinding
import cit.edu.wildcanteen.pages.ChatConversationActivity
import cit.edu.wildcanteen.pages.OrderBatchDetailActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class AcceptedOrdersFragment : Fragment() {
    private var _binding: FragmentAcceptedOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DeliveryOrderAdapter
    private var orderBatchListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcceptedOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadAcceptedOrders()
    }

    private fun setupRecyclerView() {
        adapter = DeliveryOrderAdapter(
            onItemClick = { batch ->
                showOrderDetails(batch)
            },
            onAcceptClick = { batch ->
                chatBuyer(batch)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun loadAcceptedOrders() {
        binding.progressBar.visibility = View.VISIBLE
        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = null,
            onUpdate = { batches ->
                binding.progressBar.visibility = View.GONE
                val acceptedOrders = batches.filter { batch ->
                    batch.status == "Delivering" || batch.status == "Completed" &&
                            batch.deliveredBy == MyApplication.studentId
                }.sortedByDescending { it.timestamp }

                adapter.submitList(acceptedOrders)

                if (acceptedOrders.isEmpty()) {
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

    private fun chatBuyer(batch: OrderBatch) {
        val repo = FirebaseRepository()

        repo.getUserProfileImageUrl(batch.userId, onSuccess = { recipientImageUrl ->
            if (recipientImageUrl != null) {

                val intent = Intent(context, ChatConversationActivity::class.java).apply {
                    putExtra("senderId", MyApplication.studentId)
                    putExtra("senderName", MyApplication.name)
                    putExtra("senderImage", MyApplication.profileImageUrl)
                    putExtra("recipientId", batch.userId)
                    putExtra("recipientName", batch.userName)
                    putExtra("recipientImage", recipientImageUrl)
                }
                context?.startActivity(intent)
            }
        }, onFailure = {
            Toast.makeText(requireContext(), "Failed to load recipient image", Toast.LENGTH_SHORT).show()
        })

    }

}