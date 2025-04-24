package cit.edu.wildcanteen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.bumptech.glide.Glide

class FeedbackPagerAdapter(
    private val context: Context,
    private val orders: List<Order>
) : RecyclerView.Adapter<FeedbackPagerAdapter.FeedbackViewHolder>() {

    private val firebaseRepository = FirebaseRepository()
    private val submittedPositions = mutableSetOf<Int>()

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val feedbackEditText: EditText = itemView.findViewById(R.id.feedbackEditText)
        val submitButton: Button = itemView.findViewById(R.id.submitButton)
        val submittedIndicator: TextView = itemView.findViewById(R.id.submittedIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_feedback_form, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val order = orders[position]
        val foodItem = order.items

        holder.foodName.text = foodItem.name
        holder.foodPrice.text = "₱${"%.2f".format(foodItem.price)}"
        holder.ratingBar.rating = 0f
        holder.feedbackEditText.text.clear()

        // Check if feedback was already submitted for this position
        if (submittedPositions.contains(position)) {
            showSubmittedState(holder)
            return
        } else {
            showEditableState(holder)
        }

        if (foodItem.imageUrl.startsWith("http")) {
            Glide.with(context)
                .load(foodItem.imageUrl)
                .into(holder.foodImage)
        } else {
            val resourceId = context.resources.getIdentifier(
                foodItem.imageUrl,
                "drawable",
                context.packageName
            )
            holder.foodImage.setImageResource(
                if (resourceId != 0) resourceId else R.drawable.chicken
            )
        }

        holder.submitButton.setOnClickListener {
            val rating = holder.ratingBar.rating.toDouble()
            val feedbackText = holder.feedbackEditText.text.toString().trim()

            if (rating == 0.0) {
                Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (feedbackText.isBlank()) {
                Toast.makeText(context, "Please enter your feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitFeedback(holder, position, foodItem.name, rating, feedbackText)
        }
    }

    private fun submitFeedback(
        holder: FeedbackViewHolder,
        position: Int,
        foodName: String,
        rating: Double,
        feedbackText: String
    ) {
        val feedback = Feedback(
            foodId = orders[position].items.foodId,
            userId = MyApplication.studentId!!,
            name = MyApplication.name!!,
            profileImageUrl = MyApplication.profileImageUrl!!,
            rating = rating,
            imageUrl = emptyList(),
            feedback = feedbackText,
            likes = 0,
            disLikes = 0,
            timestamp = System.currentTimeMillis()
        )

        firebaseRepository.addFeedback(
            feedback = feedback,
            onSuccess = {
                submittedPositions.add(position)
                showSubmittedState(holder)
                Toast.makeText(context, "Feedback submitted for $foodName", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(
                    context,
                    "Failed to submit feedback: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun showSubmittedState(holder: FeedbackViewHolder) {
        holder.ratingBar.isEnabled = false
        holder.feedbackEditText.isEnabled = false
        holder.submitButton.visibility = View.GONE
        holder.submittedIndicator.visibility = View.VISIBLE
    }

    private fun showEditableState(holder: FeedbackViewHolder) {
        holder.ratingBar.isEnabled = true
        holder.feedbackEditText.isEnabled = true
        holder.submitButton.visibility = View.VISIBLE
        holder.submittedIndicator.visibility = View.GONE
    }

    override fun getItemCount() = orders.size
}