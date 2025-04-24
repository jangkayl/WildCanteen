package cit.edu.wildcanteen.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class FeedbackAdapter(
    private var feedbacks: List<Feedback>,
    private val currentUserId: String,
    private val onDeleteClicked: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val userRating: RatingBar = view.findViewById(R.id.userRating)
        val userImage: ImageView = view.findViewById(R.id.user_avatar)
        val feedbackText: TextView = view.findViewById(R.id.feedbackText)
        val feedbackDate: TextView = view.findViewById(R.id.feedbackDate)
        val deleteFeedback: ImageView = view.findViewById(R.id.deleteFeedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbacks[position]

        holder.deleteFeedback.visibility = if (feedback.userId == currentUserId) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.deleteFeedback.setOnClickListener {
            onDeleteClicked(feedback)
        }

        holder.userName.text = feedback.name
        holder.userRating.rating = feedback.rating.toFloat()
        holder.feedbackText.text = feedback.feedback
        holder.feedbackDate.text = formatDate(feedback.timestamp)

        Glide.with(holder.itemView.context)
            .load(feedback.profileImageUrl)
            .placeholder(R.drawable.hd_user)
            .error(R.drawable.hd_user)
            .circleCrop()
            .into(holder.userImage)

        when {
            feedback.rating >= 4.5 -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_positive)
            feedback.rating >= 3.0 -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_neutral)
            else -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_negative)
        }
    }

    fun updateFeedbacks(newFeedbacks: List<Feedback>) {
        this.feedbacks = newFeedbacks
        notifyDataSetChanged()
    }

    override fun getItemCount() = feedbacks.size

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}