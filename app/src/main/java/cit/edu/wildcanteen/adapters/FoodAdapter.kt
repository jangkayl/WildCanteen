package cit.edu.wildcanteen.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.pages.FoodDetailsActivity
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.R
import com.bumptech.glide.Glide

class FoodAdapter(
    private var foodList: MutableList<FoodItem>,
    private val context: Context,
    private val layoutResId: Int
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView? = itemView.findViewById(R.id.foodName)
        val foodRating: TextView = itemView.findViewById(R.id.foodRating)
        val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        val foodImage: ImageView = itemView.findViewById(R.id.foodImage)

        init {
            itemView.setOnClickListener {
                val food = foodList[adapterPosition]
                val intent = Intent(context, FoodDetailsActivity::class.java).apply {
                    putExtra("FOOD_CATEGORY", food.category)
                    putExtra("FOOD_CANTEEN_ID", food.canteenId)
                    putExtra("FOOD_ID", food.foodId.toString())
                    putExtra("FOOD_NAME", food.name)
                    putExtra("FOOD_PRICE", food.price.toString())
                    putExtra("FOOD_RATING", food.rating.toString())
                    putExtra("FOOD_CANTEEN", food.canteenName)
                    putExtra("FOOD_DESCRIPTION", food.description)
                    putExtra("FOOD_IMAGE", food.imageUrl)
                    putExtra("FOOD_POPULAR", food.isPopular)
                }
                context.startActivity(intent)
            }
        }
    }

    fun updateFoodList(newFoodList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newFoodList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]

        Glide.with(holder.itemView.context)
            .load(food.imageUrl)
            .into(holder.foodImage)

        holder.foodPrice.text = String.format("₱%.2f", food.price)
        holder.foodRating.text = "⭐ ${food.rating}"

        if (layoutResId != R.layout.food_item_popular) {
            holder.foodName?.text = food.name
        }
    }

    override fun getItemCount() = foodList.size
}
