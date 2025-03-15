package cit.edu.wildcanteen

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(private val foodList: List<FoodItem>, private val context: Context) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        val foodRating: TextView = itemView.findViewById(R.id.foodRating)

        init {
            itemView.setOnClickListener {
                val food = foodList[adapterPosition]
                val intent = Intent(context, FoodDetailsActivity::class.java).apply {
                    putExtra("FOOD_NAME", food.name)
                    putExtra("FOOD_PRICE", food.price)
                    putExtra("FOOD_RATING", food.rating)
                    putExtra("FOOD_DESCRIPTION", food.description)
                    putExtra("FOOD_IMAGE", food.imageResId)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.food_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.foodImage.setImageResource(food.imageResId)
        holder.foodName.text = food.name
        holder.foodPrice.text = food.price
        holder.foodRating.text = "⭐ ${food.rating}"
    }

    override fun getItemCount() = foodList.size
}
