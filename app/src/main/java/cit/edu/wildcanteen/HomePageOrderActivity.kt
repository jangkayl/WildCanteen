package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

class HomePageOrderActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_main)

        val settingsButton = findViewById<ImageView>(R.id.settings_button)
        settingsButton?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val homeButton = findViewById<ImageView>(R.id.home_button)
        homeButton?.setOnClickListener {
            finish()
        }

        val ordersButton = findViewById<ImageView>(R.id.orders_button)
        ordersButton?.setOnClickListener {
//            val intent = Intent(this, OrdersActivity::class.java)
//            startActivity(intent)
        }

        val chatsButton = findViewById<ImageView>(R.id.chats_button)
        chatsButton?.setOnClickListener {
//            val intent = Intent(this, ChatsActivity::class.java)
//            startActivity(intent)
        }
    }
}
