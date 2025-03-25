package cit.edu.wildcanteen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildcanteen.application.MyApplication

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CHECK USER LOGGED IN USING MyApplication
        if (MyApplication.isLoggedIn) {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.login)

        val registerButton: TextView = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val loginButton: Button = findViewById(R.id.loginButton)
        val idNumber: EditText = findViewById(R.id.idNumber)
        val password: EditText = findViewById(R.id.password)
        val togglePassword: ImageView = findViewById(R.id.togglePassword)

        val storedId = intent.getStringExtra("ID")
        val storedPassword = intent.getStringExtra("PASSWORD")

        idNumber.setText(storedId)

        loginButton.setOnClickListener {
            val id = idNumber.text.toString().trim()
            val pass = password.text.toString().trim()

            if (!validateInput(id, pass)) {
                return@setOnClickListener
            }

            if (id == storedId && pass == storedPassword) {
                // SAVE LOGIN STATE
                MyApplication.isLoggedIn = true
                MyApplication.saveUserDetails(
                    id,
                    MyApplication.name ?: "",
                    MyApplication.profilePic ?: R.drawable.hd_user,
                    pass
                )

                val intent = Intent(this, HomePageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        var isPasswordVisible = false
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.view)
            } else {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.hide)
            }

            password.setSelection(password.text.length)
        }
    }

    private fun validateInput(id: String, pass: String): Boolean {
        if (id.isEmpty()) {
            showToast("Student ID number cannot be empty")
            return false
        }

        if (pass.isEmpty()) {
            showToast("Password cannot be empty")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
