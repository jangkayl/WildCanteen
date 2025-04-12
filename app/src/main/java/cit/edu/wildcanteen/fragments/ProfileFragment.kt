package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import cit.edu.wildcanteen.pages.DevelopersActivity
import cit.edu.wildcanteen.pages.EditProfileActivity
import cit.edu.wildcanteen.pages.LogoutActivity
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ProfileFragment : Fragment() {
    private lateinit var username: TextView
    private lateinit var userId: TextView
    private lateinit var profileImage: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        username = view.findViewById(R.id.settings_username)
        userId = view.findViewById(R.id.settings_userId)
        profileImage = view.findViewById(R.id.profile_image)

        username.text = MyApplication.name
        userId.text = MyApplication.stringStudentId

        if (!MyApplication.profileImageUrl.isNullOrEmpty()) {
            Log.e("ProfileImage", MyApplication.profileImageUrl!!)
            Glide.with(this)
                .load(MyApplication.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .into(profileImage)
        }

        val logoutButton = view.findViewById<LinearLayout>(R.id.logout_button)
        logoutButton.setOnClickListener {
            val intent = Intent(requireContext(), LogoutActivity::class.java)
            startActivity(intent)
        }

        val editProfileButton = view.findViewById<LinearLayout>(R.id.editProfile_button)
        editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        val developersButton = view.findViewById<LinearLayout>(R.id.developers_button)
        developersButton.setOnClickListener {
            val intent = Intent(requireContext(), DevelopersActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        username.text = MyApplication.name
        userId.text = MyApplication.stringStudentId
        if (!MyApplication.profileImageUrl.isNullOrEmpty()) {
            Log.e("ProfileImage", MyApplication.profileImageUrl!!)
            Glide.with(this)
                .load(MyApplication.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .into(profileImage)
        }
    }
}
