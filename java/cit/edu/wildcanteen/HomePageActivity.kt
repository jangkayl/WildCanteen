package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView

class HomePageActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        val settingsButton = findViewById<ImageView>(R.id.settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val homeButton = findViewById<ImageView>(R.id.home_button)
        val scrollView = findViewById<ScrollView>(R.id.homeScrollView)

        homeButton.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }

        val orderHomePage = findViewById<View>(R.id.homepageOrder)
        orderHomePage.setOnClickListener {
            val intent = Intent(this, HomePageOrderActivity::class.java)
            startActivity(intent)
        }
    }
}
