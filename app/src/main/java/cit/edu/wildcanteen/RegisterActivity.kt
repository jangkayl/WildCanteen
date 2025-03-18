package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val studentIdEditText: EditText = findViewById(R.id.studentID)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPassword)
        val createAccountButton: Button = findViewById(R.id.create_account_button)
        val loginButton: TextView = findViewById(R.id.login_button)

        passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        confirmPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        createAccountButton.setOnClickListener {
            val studentId = studentIdEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (studentId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                studentIdEditText.error = "Required"
                passwordEditText.error = "Required"
                confirmPasswordEditText.error = "Required"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Passwords do not match"
                return@setOnClickListener
            }

            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            intent.putExtra("PASSWORD", password)
            Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_LONG).show()
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val togglePassword: ImageView = findViewById(R.id.togglePassword)
        val toggleConfirmPassword: ImageView = findViewById(R.id.toggleConfirmPassword)

        var isPasswordVisible = false
        var isConfirmPasswordVisible = false

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.view)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.hide)
            }

            passwordEditText.setSelection(passwordEditText.text.length)
        }

        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible

            if (isConfirmPasswordVisible) {
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleConfirmPassword.setImageResource(R.drawable.view)
            } else {
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleConfirmPassword.setImageResource(R.drawable.hide)
            }

            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }
    }
}
