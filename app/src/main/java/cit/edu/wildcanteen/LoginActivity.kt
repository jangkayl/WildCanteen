package cit.edu.wildcanteen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

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

        loginButton.setOnClickListener {
            val id = idNumber.text.toString().trim()
            val pass = password.text.toString().trim()

            if(id.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, "Fields should not be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (id == "12345" && pass == "password") {
                startActivity(Intent(this, HomePageActivity::class.java))
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

}
