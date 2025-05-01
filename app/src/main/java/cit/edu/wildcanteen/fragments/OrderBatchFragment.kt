package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.OrderBatchAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.student_pages.OrderBatchDetailActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class OrderBatchFragment : Fragment() {
    private lateinit var firebaseRepository: FirebaseRepository
    private var orderBatchListener: ListenerRegistration? = null
    private lateinit var adapter: OrderBatchAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private var statusFilter: String = ""

    companion object {
        fun newInstance(statusFilter: String): OrderBatchFragment {
            val fragment = OrderBatchFragment()
            val args = Bundle()
            args.putString("status_filter", statusFilter)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            statusFilter = it.getString("status_filter", "")
        }
        firebaseRepository = FirebaseRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_batch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.orderBatchesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateText = view.findViewById(R.id.emptyStateText)

        setupRecyclerView()
        loadOrderBatches()
    }

    private fun setupRecyclerView() {
        adapter = OrderBatchAdapter { batch ->
            val intent = Intent(requireContext(), OrderBatchDetailActivity::class.java).apply {
                putExtra("BATCH_ID", batch.batchId)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun loadOrderBatches() {
        progressBar.visibility = View.VISIBLE

        val userId = if (MyApplication.userType == "admin") {
            null
        } else {
            MyApplication.studentId
        }

        orderBatchListener = firebaseRepository.listenForOrderBatches(
            userId = userId,
            onUpdate = { batches ->
                progressBar.visibility = View.GONE

                val filteredBatches = when (statusFilter) {
                    "preparing" -> batches.filter { it.status.lowercase() == "preparing" }
                    "delivering_ready" -> batches.filter { it.status.lowercase() == "delivering" || it.status.lowercase() == "ready" }
                    "completed_cancelled" -> batches.filter { it.status.lowercase() == "completed" || it.status.lowercase() == "cancelled"}
                    else -> batches
                }

                adapter.submitList(filteredBatches)

                if (filteredBatches.isEmpty()) {
                    emptyStateText.visibility = View.VISIBLE
                } else {
                    emptyStateText.visibility = View.GONE
                }
            },
            onFailure = { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderBatchListener?.remove()
    }
}