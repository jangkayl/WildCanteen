package cit.edu.wildcanteen

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class FoodDetailsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        setContentView(R.layout.activity_food_details)

        val foodName = intent.getStringExtra("FOOD_NAME")
        val foodPrice = intent.getStringExtra("FOOD_PRICE")
        val foodRating = intent.getStringExtra("FOOD_RATING")
        val foodImage = intent.getIntExtra("FOOD_IMAGE", 0)
        val foodDescription = intent.getStringExtra("FOOD_DESCRIPTION")

        findViewById<TextView>(R.id.food_name).text = foodName
        findViewById<TextView>(R.id.food_price).text = foodPrice
        findViewById<TextView>(R.id.food_rating).text = "⭐ $foodRating"
        findViewById<ImageView>(R.id.foodImage).setImageResource(foodImage)
        findViewById<TextView>(R.id.foodDescription).text = foodDescription

        val quantityText = findViewById<TextView>(R.id.food_quantity)
        val btnIncrease = findViewById<Button>(R.id.btn_increase)
        val btnDecrease = findViewById<Button>(R.id.btn_decrease)
        val btnOrder = findViewById<Button>(R.id.btn_order)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        var quantity = 1
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

        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down)
    }


}
