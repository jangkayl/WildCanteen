package cit.edu.wildcanteen.pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.CloudinaryRepository
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class EditProfileActivity : Activity() {
    private val firebaseRepo = FirebaseRepository()
    private val cloudinaryRepo = CloudinaryRepository(this)
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var profileImage: ImageView
    private var selectedImageUri: Uri? = null

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
        profileImage = findViewById(R.id.profile_image)

        usernameTextView.text = MyApplication.name
        userIdTextView.text = MyApplication.stringStudentId

        if (!MyApplication.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(MyApplication.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .into(profileImage)
        }

        // Allow the user to change the profile image
        profileImage.setOnClickListener {
            selectImageFromGallery()
        }

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
            if (selectedImageUri != null) {
                uploadImageToCloudinary()
            } else {
                updateUserData(nameEditView, passwordEditText, confirmPasswordEditText, MyApplication.profileImageUrl)
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage)  // Show preview
        }
    }

    private fun uploadImageToCloudinary() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = File(getRealPathFromUri(selectedImageUri!!))
        cloudinaryRepo.uploadProfileImage(
            imageFile,
            onSuccess = { imageUrl ->
                Log.d("CloudinaryUpload", "Image uploaded: $imageUrl")
                updateUserData(
                    findViewById(R.id.fullName),
                    findViewById(R.id.password),
                    findViewById(R.id.confirmPassword),
                    imageUrl
                )
            },
            onFailure = { exception ->
                runOnUiThread {
                    Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun updateUserData(
        nameEditView: EditText,
        passwordEditText: EditText,
        confirmPasswordEditText: EditText,
        imageUrl: String?
    ) {
        val newName = nameEditView.text.toString().trim()
        val newPassword = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (newName.isEmpty() && newPassword.isEmpty() && imageUrl == MyApplication.profileImageUrl) {
            Toast.makeText(this, "No changes detected!", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>()

        if (newName.isNotEmpty() && newName != MyApplication.name) {
            updates["name"] = newName
        }

        if (newPassword.isNotEmpty()) {
            updates["password"] = newPassword
        }

        if (!imageUrl.isNullOrEmpty() && imageUrl != MyApplication.profileImageUrl) {
            updates["profileImageUrl"] = imageUrl
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

                    if (newName.isNotEmpty()) {
                        MyApplication.name = newName
                    }
                    MyApplication.profileImageUrl = imageUrl

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

    private fun getRealPathFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val realPath = cursor?.getString(index ?: 0)
        cursor?.close()
        return realPath ?: ""
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
