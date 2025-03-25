package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cit.edu.wildcanteen.application.MyApplication

class FoodDetailsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        setContentView(R.layout.activity_food_details)

        val foodCategory = intent.getStringExtra("FOOD_CATEGORY")
        val foodName = intent.getStringExtra("FOOD_NAME")
        val foodPrice = intent.getStringExtra("FOOD_PRICE")
        val foodRating = intent.getStringExtra("FOOD_RATING")
        val foodImage = intent.getIntExtra("FOOD_IMAGE", 0)
        val foodDescription = intent.getStringExtra("FOOD_DESCRIPTION")

        findViewById<TextView>(R.id.food_name).text = foodName
        if (foodPrice != null) {
            findViewById<TextView>(R.id.food_price).text = "₱${String.format("%.2f", foodPrice.toDouble())}"
        }
        findViewById<TextView>(R.id.food_rating).text = "⭐ ${foodRating}"
        findViewById<ImageView>(R.id.foodImage).setImageResource(foodImage)
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
                items = FoodItem(
                    category =  foodCategory ?: "Unknown",
                    name = foodName ?: "Unknown",
                    price = foodPrice?.toDoubleOrNull() ?: 0.0,
                    rating = foodRating?.toDoubleOrNull() ?: 0.0,
                    description = foodDescription ?: "",
                    imageResId = foodImage
                ),
                quantity = quantity,
                totalAmount = (foodPrice?.toDoubleOrNull() ?: 0.0) * quantity,
                timestamp = System.currentTimeMillis()
            )

//            Toast.makeText(this, "$foodName added to cart", Toast.LENGTH_SHORT).show()
            MyApplication.addOrder(order)

            startActivity(Intent(this, OrderPlaced::class.java))
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
    }
}
