package cit.edu.wildcanteen.pages.student_pages

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import cit.edu.wildcanteen.Order
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.adapters.FeedbackPagerAdapter
import cit.edu.wildcanteen.repositories.FirebaseRepository

class SendFeedbackActivity : AppCompatActivity() {
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var viewPager: ViewPager2
    private val orders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_feedback)

        firebaseRepository = FirebaseRepository()
        viewPager = findViewById(R.id.viewPager)

        val batchId = intent.getStringExtra("BATCH_ID") ?: run {
            Toast.makeText(this, "No batch ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadOrders(batchId)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadOrders(batchId: String) {
        firebaseRepository.getAllOrdersFromBatch(
            batchId = batchId,
            onSuccess = { ordersList ->
                orders.clear()
                orders.addAll(ordersList)
                setupViewPager()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Failed to load orders: ${exception.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    private fun setupViewPager() {
        if (orders.isEmpty()) {
            Toast.makeText(this, "No orders found in this batch", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val adapter = FeedbackPagerAdapter(this, orders)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        viewPager.setPageTransformer { page, position ->
            when {
                position < -1 -> page.alpha = 0f
                position <= 0 -> {
                    page.alpha = 1f
                    page.translationX = 0f
                    page.scaleX = 1f
                    page.scaleY = 1f
                    page.translationZ = 0f
                    page.elevation = 8f
                }
                position <= 1 -> {
                    page.alpha = 1 - position
                    page.translationX = page.width * -position * 0.5f
                    page.scaleX = 1 - position * 0.1f
                    page.scaleY = 1 - position * 0.1f
                    page.translationZ = -position * 20f
                    page.elevation = 4f
                }
                else -> page.alpha = 0f
            }
        }

        val swipeGuide = LayoutInflater.from(this).inflate(R.layout.swipe_guide, null).apply {
            alpha = 0f
            visibility = View.VISIBLE
        }
        val handIcon = swipeGuide.findViewById<ImageView>(R.id.handIcon)
        val guideText = swipeGuide.findViewById<TextView>(R.id.guideText)

        addContentView(swipeGuide, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 100
        })

        val handAnimator = ObjectAnimator.ofFloat(handIcon, "translationX", 0f, 100f, 0f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
        }

        val textAnimator = ObjectAnimator.ofPropertyValuesHolder(
            guideText,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.05f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.05f, 1f)
        ).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        swipeGuide.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .withStartAction {
                handAnimator.start()
                textAnimator.start()
            }
            .start()

        var hasSwiped = false

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffsetPixels != 0 && !hasSwiped) {
                    hasSwiped = true
                    handAnimator.cancel()
                    textAnimator.cancel()
                    swipeGuide.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction { swipeGuide.visibility = View.GONE }
                        .start()
                    viewPager.unregisterOnPageChangeCallback(this)
                }
            }
        })

        val clickOverlay = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            isFocusable = true
            setBackgroundColor(Color.TRANSPARENT)
        }

        addContentView(clickOverlay, clickOverlay.layoutParams)

        clickOverlay.setOnClickListener {
            if (!hasSwiped && swipeGuide.visibility == View.VISIBLE) {
                handAnimator.cancel()
                textAnimator.cancel()
                swipeGuide.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { swipeGuide.visibility = View.GONE }
                    .start()
            }

            (clickOverlay.parent as? FrameLayout)?.removeView(clickOverlay)
        }
    }
}
