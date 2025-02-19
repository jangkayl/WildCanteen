package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class HomePageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
        }
    }
}
