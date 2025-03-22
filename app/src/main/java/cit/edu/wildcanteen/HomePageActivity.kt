package cit.edu.wildcanteen

import android.os.Bundle
import android.view.MenuItem
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cit.edu.wildcanteen.fragments.ChatsFragment
import cit.edu.wildcanteen.fragments.HomeFragment
import cit.edu.wildcanteen.fragments.OrdersFragment
import cit.edu.wildcanteen.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)
        supportActionBar?.hide()

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    findViewById<ScrollView>(R.id.homeScrollView).smoothScrollTo(0, 0)
                    true
                }
                R.id.nav_chats -> {
                    replaceFragment(ChatsFragment())
                    true
                }
                R.id.nav_orders -> {
                    replaceFragment(OrdersFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.linear_container)

        if (currentFragment?.javaClass != fragment.javaClass) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.linear_container, fragment)
                .commit()
        }
    }
}
