package cit.edu.wildcanteen.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import cit.edu.wildcanteen.BuildConfig

class CloudinaryRepository(private val context: Context) {
    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", BuildConfig.CLOUDINARY_NAME,
            "api_key", BuildConfig.CLOUDINARY_API_KEY,
            "api_secret", BuildConfig.CLOUDINARY_SECRET_KEY
        )
    )

    private val executorService = Executors.newSingleThreadExecutor()

    fun uploadProfileImage(imageFile: File, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        executorService.execute {
            try {
                val uploadResult = cloudinary.uploader().upload(imageFile,
                    ObjectUtils.asMap("folder", "WildCanteen_profile"
                ))
                val imageUrl = uploadResult["secure_url"] as String
                Log.d("CloudinaryUpload", "Image uploaded: $imageUrl")
                onSuccess(imageUrl)
            } catch (e: Exception) {
                Log.e("CloudinaryUpload", "Upload failed", e)
                onFailure(e)
            }
        }
    }

    fun uploadFoodImage(imageFile: File, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        executorService.execute {
            try {
                val uploadResult = cloudinary.uploader().upload(imageFile,
                    ObjectUtils.asMap("folder", "WildCanteen_food"
                    ))
                val imageUrl = uploadResult["secure_url"] as String
                Log.d("CloudinaryUpload", "Image uploaded: $imageUrl")
                onSuccess(imageUrl)
            } catch (e: Exception) {
                Log.e("CloudinaryUpload", "Upload failed", e)
                onFailure(e)
            }
        }
    }
}
