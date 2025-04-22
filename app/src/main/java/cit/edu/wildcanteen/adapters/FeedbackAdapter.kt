package cit.edu.wildcanteen.adapters

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
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class FeedbackAdapter(
    private var feedbacks: List<Feedback>,
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    private val userInteractions = mutableMapOf<Int, Boolean>()

    inner class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val userRating: RatingBar = view.findViewById(R.id.userRating)
        val userImage: ImageView = view.findViewById(R.id.user_avatar)
        val feedbackText: TextView = view.findViewById(R.id.feedbackText)
        val feedbackDate: TextView = view.findViewById(R.id.feedbackDate)
        val iconLike: ImageView = view.findViewById(R.id.iconLike)
        val iconDislike: ImageView = view.findViewById(R.id.iconDislike)
        val likeCount: TextView = view.findViewById(R.id.likeCount)
        val dislikeCount: TextView = view.findViewById(R.id.dislikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbacks[position]

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

        updateLikeDislikeViews(holder, feedback, position)

        holder.iconLike.setOnClickListener {
            handleLikeClick(holder, position)
        }

        holder.iconDislike.setOnClickListener {
            handleDislikeClick(holder, position)
        }

        when {
            feedback.rating >= 4.5 -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_positive)
            feedback.rating >= 3.0 -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_neutral)
            else -> holder.itemView.setBackgroundResource(R.drawable.feedback_bg_negative)
        }
    }

    private fun handleLikeClick(holder: FeedbackViewHolder, position: Int) {
        val currentFeedback = feedbacks[position]
        val isCurrentlyLiked = userInteractions[position] == true

        if (isCurrentlyLiked) {
            currentFeedback.likes--
            userInteractions.remove(position)
            holder.iconLike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            currentFeedback.likes++
            if (userInteractions[position] == false) {
                currentFeedback.disLikes--
                holder.iconDislike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.white))
            }
            userInteractions[position] = true
            holder.iconLike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.selectedColor))
        }

        updateLikeDislikeViews(holder, currentFeedback, position)
    }

    private fun handleDislikeClick(holder: FeedbackViewHolder, position: Int) {
        val currentFeedback = feedbacks[position]
        val isCurrentlyDisliked = userInteractions[position] == false

        if (isCurrentlyDisliked) {
            currentFeedback.disLikes--
            userInteractions.remove(position)
            holder.iconDislike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            currentFeedback.disLikes++
            if (userInteractions[position] == true) {
                currentFeedback.likes--
                holder.iconLike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.white))
            }
            userInteractions[position] = false
            holder.iconDislike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.selectedColor))
        }

        updateLikeDislikeViews(holder, currentFeedback, position)
    }

    private fun updateLikeDislikeViews(holder: FeedbackViewHolder, feedback: Feedback, position: Int) {
        holder.likeCount.text = feedback.likes.toString()
        holder.dislikeCount.text = feedback.disLikes.toString()

        holder.likeCount.visibility = if (feedback.likes > 0) View.VISIBLE else View.GONE
        holder.dislikeCount.visibility = if (feedback.disLikes > 0) View.VISIBLE else View.GONE

        when (userInteractions[position]) {
            true -> holder.iconLike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.selectedColor))
            false -> holder.iconDislike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.selectedColor))
            else -> {
                holder.iconLike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.iconDislike.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.white))
            }
        }
    }

    override fun getItemCount() = feedbacks.size

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}