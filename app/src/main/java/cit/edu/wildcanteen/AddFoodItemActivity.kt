package cit.edu.wildcanteen

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cit.edu.wildcanteen.application.MyApplication
import cit.edu.wildcanteen.repositories.CloudinaryRepository
import cit.edu.wildcanteen.repositories.FirebaseRepository
import cit.edu.wildcanteen.repositories.FoodRepository
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddFoodItemActivity : AppCompatActivity() {
    private lateinit var etCategory: EditText
    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etRating: EditText
    private lateinit var etDescription: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var etPopular: CheckBox
    private lateinit var btnAddFood: Button
    private lateinit var btnSelectImage: Button
    private lateinit var ivPreview: ImageView

    private val firebaseRepository = FirebaseRepository()
    private val cloudinaryRepository by lazy { CloudinaryRepository(this) }
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_food_item)

        etCategory = findViewById(R.id.etCategory)
        etName = findViewById(R.id.etName)
        etPrice = findViewById(R.id.etPrice)
        etRating = findViewById(R.id.etRating)
        etDescription = findViewById(R.id.etDescription)
        etPopular = findViewById(R.id.cbPopular)
        etImageUrl = findViewById(R.id.etImageUrl)
        btnAddFood = findViewById(R.id.btnAddFood)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        ivPreview = findViewById(R.id.ivPreview)

        btnSelectImage.setOnClickListener {
            selectImageFromGallery()
        }

        btnAddFood.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageToCloudinary()
            } else {
                addFoodItemToDatabase(etImageUrl.text.toString().trim())
            }
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            ivPreview.setImageURI(selectedImageUri)
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver: ContentResolver = contentResolver
        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, fileName)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun uploadImageToCloudinary() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = getFileFromUri(selectedImageUri!!)
        if (imageFile == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        cloudinaryRepository.uploadFoodImage(imageFile, { imageUrl ->
            uploadedImageUrl = imageUrl
            runOnUiThread {
                addFoodItemToDatabase(imageUrl)
            }
        }, { error ->
            runOnUiThread {
                Toast.makeText(this, "Image upload failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFoodItemToDatabase(imageUrl: String) {
        val category = etCategory.text.toString().trim()
        val name = etName.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val rating = etRating.text.toString().toDoubleOrNull() ?: 0.0
        val description = etDescription.text.toString().trim()
        val isPopular = etPopular.isChecked
        val foodItemSize = MyApplication.foodItems.size

        if (category.isEmpty() || name.isEmpty() || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val foodItem = FoodItem(category,foodItemSize+1, name, price, rating, description, imageUrl, isPopular)

        firebaseRepository.addFoodItem(foodItem, {
            Toast.makeText(this, "Food item added successfully", Toast.LENGTH_SHORT).show()
            finish()
        }, { error ->
            Toast.makeText(this, "Failed to add food item: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }
}