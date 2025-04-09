package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.R

class FeedbackAdapter(private var feedbackList: List<Feedback>) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.bind(feedback)
    }

    override fun getItemCount(): Int = feedbackList.size

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val feedbackText: TextView = itemView.findViewById(R.id.feedbackText)
        private val rating: RatingBar = itemView.findViewById(R.id.ratingBar)

        fun bind(feedback: Feedback) {
            userName.text = feedback.name
            feedbackText.text = feedback.text
            rating.rating = feedback.rating.toFloat()
        }
    }
}
