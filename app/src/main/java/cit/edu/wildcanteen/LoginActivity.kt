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

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val registerButton: TextView = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val loginButton: Button = findViewById(R.id.loginButton)
        val idNumber: EditText = findViewById(R.id.idNumber)
        val password: EditText = findViewById(R.id.password)

        password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val studentId1 = intent.getStringExtra("STUDENT_ID") ?: ""
        val password1 = intent.getStringExtra("PASSWORD") ?: ""

        idNumber.setText(studentId1)

        loginButton.setOnClickListener {
            val id = idNumber.text.toString().trim()
            val pass = password.text.toString().trim()

//            if(id.isEmpty() || pass.isEmpty()){
//                Toast.makeText(this, "Fields should not be empty!", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }

            if (id == studentId1 && pass == password1 || id == "" && pass == "") {
                startActivity(Intent(this, HomePageActivity::class.java))
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

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

    }

}
