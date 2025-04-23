package cit.edu.wildcanteen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import com.bumptech.glide.Glide

class FeedbackPagerAdapter(
    private val context: Context,
    private val orders: List<Order>
) : RecyclerView.Adapter<FeedbackPagerAdapter.FeedbackViewHolder>() {
    private val ratings = mutableMapOf<Int, Float>()
    private val feedbacks = mutableMapOf<Int, String>()

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val feedbackEditText: EditText = itemView.findViewById(R.id.feedbackEditText)
        val submitButton: Button = itemView.findViewById(R.id.submitButton)
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

        if (foodItem.imageUrl.startsWith("http")) {
            Glide.with(context)
                .load(foodItem.imageUrl)
                .placeholder(R.drawable.chicken)
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
            val feedbackText = holder.feedbackEditText.text.toString()

            if (feedbackText.isBlank()) {
                Toast.makeText(context, "Please enter your feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(context, "Feedback submitted for ${foodItem.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = orders.size
}