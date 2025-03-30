package cit.edu.wildcanteen

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildcanteen.application.MyApplication
import java.security.MessageDigest
import android.util.Base64
import cit.edu.wildcanteen.repositories.FirebaseRepository

class LoginActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(MyApplication.isLoggedIn){
            finish()
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

        loginButton.setOnClickListener {
            val id = idNumber.text.toString().trim()
            val pass = password.text.toString().trim()

            if (!validateInput(id, pass)) {
                return@setOnClickListener
            }

            authenticateUser(id, pass)
        }
    }

    private fun authenticateUser(userId: String, password: String) {
        firebaseRepository.getUser(userId, { user ->
            if (user != null) {
                val hashedInputPassword = hashPassword(password)

                if (hashedInputPassword == user.password) {
                    MyApplication.loadUserDetails(user)
                    startActivity(Intent(this, HomePageActivity::class.java))
                    finish()
                } else {
                    showToast("Invalid credentials")
                }
            } else {
                showToast("User not found")
            }
        }, { exception ->
            showToast("Error: ${exception.message}")
        })
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
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
