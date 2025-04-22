package cit.edu.wildcanteen.pages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.FeedbackAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.FirebaseRepository
import cit.edu.wildcanteen.repositories.StaticRepository
import com.bumptech.glide.Glide

class FoodDetailsActivity : Activity() {

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var feedbackAdapter: FeedbackAdapter
    private lateinit var foodId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        setContentView(R.layout.activity_food_details)

        firebaseRepository = FirebaseRepository()

        val foodCategory = intent.getStringExtra("FOOD_CATEGORY")
        val foodName = intent.getStringExtra("FOOD_NAME")
        foodId = intent.getStringExtra("FOOD_ID") ?: ""
        val foodPrice = intent.getStringExtra("FOOD_PRICE")
        val foodRating = intent.getStringExtra("FOOD_RATING")
        val foodImage = intent.getStringExtra("FOOD_IMAGE")
        val foodDescription = intent.getStringExtra("FOOD_DESCRIPTION")
        val foodPopular = intent.getBooleanExtra("FOOD_POPULAR", false)
        val foodCanteen = intent.getStringExtra("FOOD_CANTEEN") + " Canteen"
        val foodCanteenId = intent.getStringExtra("FOOD_CANTEEN_ID")
        val foodFeedbacks = intent.getSerializableExtra("FOOD_FEEDBACKS") as? List<Feedback>

        findViewById<TextView>(R.id.food_name).text = foodName
        if (foodPrice != null) {
            findViewById<TextView>(R.id.food_price).text = "₱${String.format("%.2f", foodPrice.toDouble())}"
        }
        findViewById<TextView>(R.id.food_rating).text = "⭐ ${foodRating}"

        val foodImageView = findViewById<ImageView>(R.id.foodImage)
        if (!foodImage.isNullOrEmpty()) {
            if (foodImage.startsWith("http")) {
                Glide.with(this)
                    .load(foodImage)
                    .into(foodImageView)
            } else {
                val resourceId = resources.getIdentifier(foodImage, "drawable", packageName)
                if (resourceId != 0) {
                    foodImageView.setImageResource(resourceId)
                } else {
                    foodImageView.setImageResource(R.drawable.chicken)
                }
            }
        }
        findViewById<TextView>(R.id.foodDescription).text = foodDescription

        val quantityText = findViewById<TextView>(R.id.food_quantity)
        val btnIncrease = findViewById<Button>(R.id.btn_increase)
        val btnDecrease = findViewById<Button>(R.id.btn_decrease)
        val btnOrder = findViewById<Button>(R.id.btn_order)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        val existingOrder = MyApplication.orders.find { it.items.name == foodName }
        var quantity = existingOrder?.quantity ?: 1
        quantityText.text = quantity.toString()

        btnBack.setOnClickListener {
            finish()
        }

        btnIncrease.setOnClickListener {
            quantity++
            quantityText.text = quantity.toString()
        }

        btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityText.text = quantity.toString()
            }
        }

        btnOrder.setOnClickListener {
            val order = Order(
                orderId = System.currentTimeMillis().toString(),
                canteenName = foodCanteen,
                canteenId = foodCanteenId!!,
                userId = MyApplication.studentId!!,
                userName = MyApplication.name!!,
                items = FoodItem(
                    category = foodCategory ?: "Unknown",
                    name = foodName ?: "Unknown",
                    price = foodPrice?.toDoubleOrNull() ?: 0.0,
                    rating = foodRating?.toDoubleOrNull() ?: 0.0,
                    canteenName = foodCanteen,
                    canteenId = foodCanteenId,
                    description = foodDescription ?: "",
                    imageUrl = foodImage ?: "",
                    isPopular = foodPopular,
                    foodId = foodId.toIntOrNull() ?: 0
                ),
                quantity = quantity,
                totalAmount = (foodPrice?.toDoubleOrNull() ?: 0.0) * quantity,
            )

            MyApplication.addOrder(order)

            startActivity(Intent(this, OrderPlaced::class.java))
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        feedbackAdapter = FeedbackAdapter(StaticRepository.staticFeedbacks())

        val recyclerView: RecyclerView = findViewById(R.id.recyclerFeedbacks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.visibility = View.VISIBLE
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = feedbackAdapter

        recyclerView.post {
            var totalHeight = 0
            for (i in 0 until feedbackAdapter.itemCount) {
                val holder = feedbackAdapter.createViewHolder(recyclerView, feedbackAdapter.getItemViewType(i))
                feedbackAdapter.bindViewHolder(holder, i)

                holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )

                val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                totalHeight += holder.itemView.measuredHeight + params.bottomMargin
            }

            recyclerView.layoutParams.height = totalHeight
            recyclerView.requestLayout()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
    }
}

