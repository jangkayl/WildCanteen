package cit.edu.wildcanteen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.Window
import cit.edu.wildcanteen.application.MyApplication

class LogoutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.logout)

        val yesButton = findViewById<Button>(R.id.yes_button)
        val noButton = findViewById<Button>(R.id.no_button)

        yesButton.setOnClickListener {
            MyApplication.clearUserSession()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        noButton.setOnClickListener {
            finish()
        }
    }
}
