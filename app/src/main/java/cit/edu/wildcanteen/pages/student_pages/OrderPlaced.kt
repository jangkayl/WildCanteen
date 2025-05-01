package cit.edu.wildcanteen.pages.student_pages

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import cit.edu.wildcanteen.R

class OrderPlaced : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.order_placed)

        val okButton = findViewById<Button>(R.id.ok_button)

        okButton.setOnClickListener{
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_right)
    }
}
