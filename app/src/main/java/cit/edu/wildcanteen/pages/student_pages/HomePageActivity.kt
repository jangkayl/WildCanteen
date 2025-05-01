package cit.edu.wildcanteen.pages.student_pages

import android.os.Bundle
import android.view.MenuItem
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.fragments.ChatsFragment
import cit.edu.wildcanteen.fragments.HomeFragment
import cit.edu.wildcanteen.fragments.OrdersFragment
import cit.edu.wildcanteen.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class HomePageActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)
        supportActionBar?.hide()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_chats -> ChatsFragment()
                R.id.nav_orders -> OrdersFragment()
                R.id.nav_settings -> ProfileFragment()
                else -> null
            }

            if (fragment != null) {
                replaceFragment(fragment)
                if (item.itemId == R.id.nav_home) {
                    findViewById<ScrollView>(R.id.homeScrollView)?.smoothScrollTo(0, 0)
                }
                true
            } else {
                false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.linear_container)

        if (currentFragment?.javaClass != fragment.javaClass) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.linear_container, fragment)
                .setReorderingAllowed(true)
                .commit()
        }
    }
}
