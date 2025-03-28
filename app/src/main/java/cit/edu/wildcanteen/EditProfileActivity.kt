package cit.edu.wildcanteen

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cit.edu.wildcanteen.application.MyApplication

class EditProfileActivity : Activity() {
    private val firebaseRepo = FirebaseRepository()
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.profile_page)

        val usernameTextView = findViewById<TextView>(R.id.username)
        val userIdTextView = findViewById<TextView>(R.id.user_id)
        val nameEditView = findViewById<EditText>(R.id.fullName)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPassword)
        val saveChangesButton = findViewById<Button>(R.id.saveChangesButton)
        val togglePassword: ImageView = findViewById(R.id.togglePassword)
        val toggleConfirmPassword: ImageView = findViewById(R.id.toggleConfirmPassword)

        usernameTextView.text = MyApplication.name
        userIdTextView.text = MyApplication.stringStudentId

        val backImage = findViewById<ImageView>(R.id.editProfile_back)
        backImage.setOnClickListener { finish() }

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordEditText, togglePassword, isPasswordVisible)
        }

        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordEditText, toggleConfirmPassword, isConfirmPasswordVisible)
        }

        saveChangesButton.setOnClickListener {
            updateUserData(nameEditView, passwordEditText, confirmPasswordEditText)
        }
    }

    private fun updateUserData(nameEditView: EditText, passwordEditText: EditText, confirmPasswordEditText: EditText) {
        val newName = nameEditView.text.toString().trim()
        val newPassword = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        if (newName == MyApplication.name && newPassword.isEmpty()) {
            Toast.makeText(this, "No changes detected!", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()

        if (newName != MyApplication.name) {
            updates["name"] = newName
        }

        if (newPassword.isNotEmpty()) {
            updates["password"] = newPassword
        }
        if (updates.isEmpty()) {
            Toast.makeText(this, "No changes detected!", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseRepo.updateUser(
            userId = MyApplication.studentId ?: return,
            updates = updates,
            onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    MyApplication.name = newName
                    MyApplication.password = newPassword
                    finish()
                }
            },
            onFailure = { exception ->
                runOnUiThread {
                    Toast.makeText(this, "Update failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.view)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.hide)
        }
        editText.setSelection(editText.text.length)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_right)
    }
}
