package cit.edu.wildcanteen.pages.admin_page

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.pages.LogoutActivity

class AdminTemporaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temporary_admin)

        val btnAddFood = findViewById<Button>(R.id.btnAddFood)
        val btnViewBatches = findViewById<Button>(R.id.btnViewBatches)
        val btnViewAllMenu = findViewById<Button>(R.id.btnViewAllMenu)
        val btnLogout = findViewById<Button>(R.id.logout_button)

        btnAddFood.setOnClickListener {
            val intent = Intent(this, AddFoodItemActivity::class.java)
            startActivity(intent)
        }

        btnViewAllMenu.setOnClickListener {
            val intent = Intent(this, AllMenuActivity::class.java)
            startActivity(intent)
        }

        btnViewBatches.setOnClickListener {
            val intent = Intent(this, OrderBatchesActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
        }
    }
}
