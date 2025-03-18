package cit.edu.wildcanteen

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView

class BottomNavigationMenu(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.menu, this, true)
        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.settings_button).setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }

        findViewById<ImageView>(R.id.home_button).setOnClickListener {
            if (context is HomePageActivity) {
                findViewById<ScrollView>(R.id.homeScrollView).smoothScrollTo(0, 0)
            } else {
                context.startActivity(Intent(context, HomePageActivity::class.java))
            }
        }

        findViewById<ImageView>(R.id.orders_button).setOnClickListener {
            context.startActivity(Intent(context, OrdersActivity::class.java))
        }

        findViewById<ImageView>(R.id.chats_button).setOnClickListener {
            context.startActivity(Intent(context, ChatsActivity::class.java))
        }
    }
}
