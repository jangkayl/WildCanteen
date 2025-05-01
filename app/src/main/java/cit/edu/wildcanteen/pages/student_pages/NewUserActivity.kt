package cit.edu.wildcanteen.pages.student_pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.CloudinaryRepository
import cit.edu.wildcanteen.repositories.FirebaseRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.File

class NewUserActivity : Activity() {
    private val firebaseRepo = FirebaseRepository()
    private val cloudinaryRepo = CloudinaryRepository(this)
    private lateinit var profileImage: ImageView
    private lateinit var nameEditText : EditText
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_left, R.anim.fade_out)
        setContentView(R.layout.new_user_page)

        val confirmButton = findViewById<Button>(R.id.btnConfirm_new_user)

        nameEditText = findViewById(R.id.editText_name)
        profileImage = findViewById(R.id.edit_profile_image)

        if (!MyApplication.profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(MyApplication.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .into(profileImage)
        }

        profileImage.setOnClickListener {
            selectImageFromGallery()
        }

        confirmButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageToCloudinary()
            } else {
                updateUserData(nameEditText, MyApplication.profileImageUrl)
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
                .into(profileImage)
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
                    findViewById(R.id.editText_name),
                    imageUrl
                )
            },
            onFailure = { exception ->
                runOnUiThread {
                    Log.e("Upload Failed","${exception.message}")
                    Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun updateUserData(
        nameEditView: EditText,
        imageUrl: String?
    ) {
        val newName = nameEditView.text.toString().trim()

        runOnUiThread {
            if (newName.isEmpty()) {
                nameEditView.error = "Name is required"
                nameEditView.requestFocus()
                return@runOnUiThread
            }

            if (newName.length < 2 || newName.length > 50) {
                nameEditView.error = "Name must be 2-50 characters"
                nameEditView.requestFocus()
                return@runOnUiThread
            }

            val nameChanged = newName != MyApplication.name
            val imageChanged = !imageUrl.isNullOrEmpty() && imageUrl != MyApplication.profileImageUrl

            if (!nameChanged && !imageChanged) {
                Toast.makeText(this, "No changes detected!", Toast.LENGTH_SHORT).show()
                return@runOnUiThread
            }

            val updates = mutableMapOf<String, Any>().apply {
                if (nameChanged) {
                    put("name", newName)
                }
                if (imageChanged) {
                    put("profileImageUrl", imageUrl!!)
                }
                put("lastUpdated", FieldValue.serverTimestamp())
            }

            val userId = MyApplication.studentId ?: run {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@runOnUiThread
            }

            firebaseRepo.updateUser(
                userId = userId,
                updates = updates,
                onSuccess = {
                    runOnUiThread {
                        MyApplication.name = newName
                        if (imageChanged) {
                            MyApplication.profileImageUrl = imageUrl
                        }

                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomePageActivity::class.java))
                        finish()
                    }
                },
                onFailure = { exception ->
                    runOnUiThread {
                        val errorMessage = when (exception) {
                            is FirebaseFirestoreException -> {
                                when (exception.code) {
                                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                        "You don't have permission to update profile"
                                    else -> "Failed to update profile"
                                }
                            }
                            else -> "Update failed: ${exception.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }

    private fun getRealPathFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val realPath = cursor?.getString(index ?: 0)
        cursor?.close()
        return realPath ?: ""
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_right)
    }
}
