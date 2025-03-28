package cit.edu.wildcanteen.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import cit.edu.wildcanteen.DevelopersActivity
import cit.edu.wildcanteen.EditProfileActivity
import cit.edu.wildcanteen.LogoutActivity
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.application.MyApplication

class ProfileFragment : Fragment() {
    private lateinit var username: TextView
    private lateinit var userId: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        username = view.findViewById(R.id.settings_username)
        userId = view.findViewById(R.id.settings_userId)

        username.text = MyApplication.name
        userId.text = MyApplication.stringStudentId

        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            val intent = Intent(requireContext(), LogoutActivity::class.java)
            startActivity(intent)
        }

        val editProfileButton = view.findViewById<Button>(R.id.editProfile_button)
        editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        val developersButton = view.findViewById<Button>(R.id.developers_button)
        developersButton.setOnClickListener {
            val intent = Intent(requireContext(), DevelopersActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        username.text = MyApplication.name
    }
}
