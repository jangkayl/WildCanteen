package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class DevelopersActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.developer_page)

        val backButton = findViewById<ImageView>(R.id.developer_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_right)
    }
}