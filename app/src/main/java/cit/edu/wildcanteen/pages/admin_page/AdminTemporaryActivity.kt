package cit.edu.wildcanteen.pages.admin_page

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildcanteen.R

class AdminTemporaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temporary_admin)

        val btnAddFood = findViewById<Button>(R.id.btnAddFood)
        val btnViewOrders = findViewById<Button>(R.id.btnViewOrders)

        btnAddFood.setOnClickListener {
            val intent = Intent(this, AddFoodItemActivity::class.java)
            startActivity(intent)
        }

        btnViewOrders.setOnClickListener {
            val intent = Intent(this, AllOrderActivity::class.java)
            startActivity(intent)
        }
    }
}
