package cit.edu.wildcanteen.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.FoodDetailsActivity
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.R

class FoodAdapter(
    private val foodList: List<FoodItem>,
    private val context: Context,
    private val layoutResId: Int
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        val foodRating: TextView = itemView.findViewById(R.id.foodRating)
        val foodName: TextView? = itemView.findViewById(R.id.foodName)

        init {
            itemView.setOnClickListener {
                val food = foodList[adapterPosition]
                val intent = Intent(context, FoodDetailsActivity::class.java).apply {
                    putExtra("FOOD_NAME", food.name)
                    putExtra("FOOD_PRICE", food.price.toString())
                    putExtra("FOOD_RATING", food.rating.toString())
                    putExtra("FOOD_DESCRIPTION", food.description)
                    putExtra("FOOD_IMAGE", food.imageResId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.foodImage.setImageResource(food.imageResId)
        holder.foodPrice.text = String.format("₱%.2f", food.price)
        holder.foodRating.text = "⭐ ${food.rating}"

        if (layoutResId != R.layout.food_item_popular) {
            holder.foodName?.text = food.name
        }
    }

    override fun getItemCount() = foodList.size
}
