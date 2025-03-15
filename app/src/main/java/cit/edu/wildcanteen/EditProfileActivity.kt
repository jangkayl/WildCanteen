package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class EditProfileActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        val backImage = findViewById<ImageView>(R.id.editProfile_back)
        backImage.setOnClickListener {
            finish()
        }
    }
}