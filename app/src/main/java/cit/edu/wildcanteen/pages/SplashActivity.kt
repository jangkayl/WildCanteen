package cit.edu.wildcanteen.pages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import cit.edu.wildcanteen.R

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashImage: ImageView = findViewById(R.id.splashImage)
        val splashText: TextView = findViewById(R.id.splashText)
        val splashText2: TextView = findViewById(R.id.splashText2)

        // Move image from left to center
        val imageAnim = TranslateAnimation(-500f, 0f, 0f, 0f).apply {
            duration = 1000
            fillAfter = true
        }

        // Move splashText from right to center
        val textAnim = TranslateAnimation(500f, 0f, 0f, 0f).apply {
            duration = 1000
            fillAfter = true
        }

        // Move splashText2 from bottom to its position
        val textAnim2 = TranslateAnimation(0f, 0f, 300f, 0f).apply {
            duration = 1000
            fillAfter = true
        }

        splashImage.startAnimation(imageAnim)
        splashText.startAnimation(textAnim)
        splashText2.startAnimation(textAnim2)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)

    }
}
