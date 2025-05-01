package cit.edu.wildcanteen.pages

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.Feedback
import cit.edu.wildcanteen.FoodItem
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.FeedbackAdapter
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.pages.student_pages.OrderPlaced
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ListenerRegistration

class FoodDetailsActivity : Activity() {

    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var feedbackAdapter: FeedbackAdapter
    private lateinit var foodId: String
    private lateinit var feedbacks: List<Feedback>
    private var feedbackListenerRegistration: ListenerRegistration? = null


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
        findViewById<TextView>(R.id.canteen).text = foodCanteen

        val quantityText = findViewById<TextView>(R.id.food_quantity)
        val btnIncrease = findViewById<Button>(R.id.btn_increase)
        val btnDecrease = findViewById<Button>(R.id.btn_decrease)
        val btnOrder = findViewById<Button>(R.id.btn_order)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val existingOrder = MyApplication.orders.find { it.items.name == foodName }
        var quantity = existingOrder?.quantity ?: 1
        quantityText.text = quantity.toString()
        feedbacks = emptyList()

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

        if(MyApplication.userType == "admin"){
            val deleteIcon = findViewById<ImageView>(R.id.delete_icon)

            findViewById<RelativeLayout>(R.id.bottom_button_container).visibility = View.GONE
            findViewById<LinearLayout>(R.id.linearLayout).visibility = View.GONE

            findViewById<RelativeLayout>(R.id.delete_button).visibility = View.VISIBLE
            deleteIcon.visibility = View.VISIBLE
            deleteIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.white)
            )

            findViewById<Button>(R.id.btn_delete).setOnClickListener {
                showDeleteFoodConfirmationDialog(
                    foodId = foodId,
                    foodName = foodName ?: "this item"
                )
            }

            val foodDetails = findViewById<RelativeLayout>(R.id.foodDetails)
            val params = foodDetails.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.BELOW, R.id.foodImage)
            foodDetails.layoutParams = params
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
        val recyclerView: RecyclerView = findViewById(R.id.recyclerFeedbacks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
        feedbackAdapter = FeedbackAdapter(feedbacks, MyApplication.studentId!!) { feedbackToDelete ->
            showDeleteConfirmationDialog(feedbackToDelete)
        }

        recyclerView.adapter = feedbackAdapter

        feedbackListenerRegistration = firebaseRepository.getFeedbacksForFoodListener(
            foodId = foodId.toInt(),
            onFeedbackReceived = { fetchedFeedbacks ->
                feedbacks = fetchedFeedbacks
                feedbackAdapter.updateFeedbacks(fetchedFeedbacks)

                if (fetchedFeedbacks.isEmpty()) {
                    findViewById<TextView>(R.id.noFeedbackText).visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    findViewById<TextView>(R.id.noFeedbackText).visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

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
            },
            onError = { exception ->
                Toast.makeText(this, "Error loading feedbacks: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showDeleteConfirmationDialog(feedback: Feedback) {
        AlertDialog.Builder(this)
            .setTitle("Delete Feedback")
            .setMessage("Are you sure you want to delete this feedback?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFeedback(feedback)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteFoodConfirmationDialog(foodId: String, foodName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Food Item")
            .setMessage("Are you sure you want to delete $foodName? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteFoodItem(foodId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFoodItem(foodId: String) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Deleting food item...")
            setCancelable(false)
            show()
        }

        firebaseRepository.deleteFoodItem(
            foodId = foodId.toInt(),
            onSuccess = {
                progressDialog.dismiss()
                Toast.makeText(this, "Food item deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { exception ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to delete food item: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun deleteFeedback(feedback: Feedback) {
        firebaseRepository.deleteFeedback(feedback.feedbackId, {
            val updatedList = feedbacks.toMutableList().apply { remove(feedback) }
            feedbackAdapter.updateFeedbacks(updatedList)
            Toast.makeText(this, "Feedback deleted", Toast.LENGTH_SHORT).show()
        }, { e ->
            Toast.makeText(this, "Failed to delete feedback: ${e.message}", Toast.LENGTH_SHORT).show()
        })
    }

    override fun finish() {
        super.finish()
        feedbackListenerRegistration?.remove()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
    }
}

