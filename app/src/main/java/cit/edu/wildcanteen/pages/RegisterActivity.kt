package cit.edu.wildcanteen.pages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.User
import cit.edu.wildcanteen.repositories.FirebaseRepository

class RegisterActivity : Activity() {
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.register)

        val studentIdEditText: EditText = findViewById(R.id.studentID)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPassword)
        val createAccountButton: Button = findViewById(R.id.create_account_button)
        val loginButton: TextView = findViewById(R.id.login_button)
        val togglePassword: ImageView = findViewById(R.id.togglePassword)
        val toggleConfirmPassword: ImageView = findViewById(R.id.toggleConfirmPassword)

        var isPasswordVisible = false
        var isConfirmPasswordVisible = false

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordEditText, togglePassword, isPasswordVisible)
        }

        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordEditText, toggleConfirmPassword, isConfirmPasswordVisible)
        }

        loginButton.setOnClickListener {
            finish()
        }

        createAccountButton.setOnClickListener {
            val studentId = studentIdEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (!validateInput(studentId, password, confirmPassword)) {
                return@setOnClickListener
            }

            firebaseRepository.getUser(studentId, { existingUser ->
                if (existingUser != null) {
                    showToast("Student ID is already registered")
                } else {
                    registerUser(studentId, password)
                }
            }, { error ->
                showToast("Error: ${error.message}")
            })
        }
    }

    private fun registerUser(studentId: String, password: String) {
        val user = User(studentId, "", "John Doe", password, "student")

        firebaseRepository.addUser(user, {
            showToast("Registered Successfully!")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, { error ->
            showToast("Error: ${error.message}")
        })
    }

    private fun validateInput(studentId: String, password: String, confirmPassword: String): Boolean {
        if (studentId.isEmpty()) {
            showToast("Student ID cannot be empty")
            return false
        }

        if (!studentId.matches(Regex("\\d{9,}"))) {
            showToast("Invalid Student ID. Must be at least 9 digits")
            return false
        }

        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return false
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
