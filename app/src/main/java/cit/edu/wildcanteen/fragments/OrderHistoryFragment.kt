package cit.edu.wildcanteen.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.OrderHistoryAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.firebase.firestore.ListenerRegistration

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderHistoryAdapter
    private var orderBatchListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderHistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        val userId = MyApplication.studentId ?: return

        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = userId,
            onUpdate = { batches ->
                val historyBatches = batches.filter {
                    it.status == "Completed" || it.status == "Cancelled"
                }
                adapter.submitList(historyBatches)
            },
            onFailure = { e ->
                Log.e("OrderHistoryFragment", "Error loading order history", e)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderBatchListener?.remove()
    }
}