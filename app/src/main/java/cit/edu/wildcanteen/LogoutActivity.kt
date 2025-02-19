package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class LogoutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logout)

        val yesButton = findViewById<Button>(R.id.yes_button)
        val noButton = findViewById<Button>(R.id.no_button)

        yesButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        noButton.setOnClickListener {
            finish()
        }
    }
}
