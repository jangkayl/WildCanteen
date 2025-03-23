package cit.edu.wildcanteen.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.CategoryItem
import cit.edu.wildcanteen.R

class CategoryAdapter(private val categories: List<CategoryItem>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frameLayout: FrameLayout = itemView.findViewById(R.id.frameLayout)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        val layoutParams = view.layoutParams
        val screenWidth = parent.context.resources.displayMetrics.widthPixels
        layoutParams.width = (screenWidth / 2.5).toInt()
        view.layoutParams = layoutParams

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        val drawable = ContextCompat.getDrawable(holder.itemView.context,
            R.drawable.rounded_drawable
        ) as GradientDrawable

        drawable.setColor(category.backgroundColor)

        holder.frameLayout.background = drawable

        holder.imageView.setImageResource(category.imageRes)
        holder.textView.text = category.name
    }

    override fun getItemCount() = categories.size
}
