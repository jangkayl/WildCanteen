package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.OrderedBatchesAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.student_pages.OrderBatchDetailActivity
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ListenerRegistration

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderedBatchesAdapter
    private lateinit var noOrderLayout: LinearLayout
    private lateinit var exploreButton: Button
    private lateinit var progressBar: View
    private var orderBatchListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        noOrderLayout = view.findViewById(R.id.no_order_layout)
        exploreButton = view.findViewById(R.id.explore_button)
        progressBar = view.findViewById(R.id.progressBar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderedBatchesAdapter { batch ->
            val intent = Intent(requireContext(), OrderBatchDetailActivity::class.java).apply {
                putExtra("BATCH_ID", batch.batchId)
            }
            startActivity(intent)
        }

        exploreButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.linear_container, HomeFragment())
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        recyclerView.setHasFixedSize(true)

        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        val userId = MyApplication.studentId ?: return

        progressBar.visibility = View.VISIBLE

        orderBatchListener = FirebaseRepository().listenForOrderBatches(
            userId = userId,
            onUpdate = { batches ->
                progressBar.visibility = View.GONE

                val historyBatches = batches.filter {
                    it.status == "Completed" || it.status == "Cancelled"
                }.sortedByDescending { it.timestamp }

                adapter.submitList(historyBatches)

                noOrderLayout.visibility = if (historyBatches.isEmpty()) View.VISIBLE else View.GONE
            },
            onFailure = { e ->
                Log.e("OrderHistoryFragment", "Error loading order history", e)
                progressBar.visibility = View.GONE
                noOrderLayout.visibility = View.VISIBLE
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderBatchListener?.remove()
    }
}
