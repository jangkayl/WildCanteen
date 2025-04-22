package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.R

class FeedbackAdapter(private val feedbacks: List<Feedback>) :
    RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val userRating: TextView = view.findViewById(R.id.userRating)
        val feedbackText: TextView = view.findViewById(R.id.feedbackText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbacks[position]

        holder.userName.text = feedback.name
        holder.userRating.text = "⭐ ${feedback.rating}"
        holder.feedbackText.text = feedback.feedback

        when {
            feedback.rating >= 4.5 -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_positive)
            feedback.rating >= 3.0 -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_neutral)
            else -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_negative)
        }
    }

    override fun getItemCount() = feedbacks.size
}