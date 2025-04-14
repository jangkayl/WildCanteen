package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.OrderedBatchesAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.OrderBatchDetailActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class OrderedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderedBatchesAdapter
    private lateinit var emptyView: TextView
    private var orderBatchListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ordered, container, false)
        recyclerView = view.findViewById(R.id.orderedRecyclerView)
        emptyView = view.findViewById(R.id.emptyText)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderedBatchesAdapter { batch ->
            // Open detailed invoice when an order is clicked
            val intent = Intent(requireContext(), OrderBatchDetailActivity::class.java).apply {
                putExtra("BATCH_ID", batch.batchId)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        loadOrderedBatches()
    }

    private fun loadOrderedBatches() {
        val userId = MyApplication.studentId ?: return

        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = userId,
            onUpdate = { batches ->
                val pendingBatches = batches.filter {
                    it.status == "Pending" || it.status == "Preparing" || it.status == "Ready"
                }.sortedByDescending { it.timestamp }

                adapter.submitList(pendingBatches)

                if (pendingBatches.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "No current orders"
                } else {
                    emptyView.visibility = View.GONE
                }
            },
            onFailure = { e ->
                Log.e("OrderedFragment", "Error loading ordered batches", e)
                emptyView.visibility = View.VISIBLE
                emptyView.text = "Error loading orders"
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderBatchListener?.remove()
    }
}