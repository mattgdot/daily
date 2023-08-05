package com.app.daily.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.daily.R
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.databinding.ActivityMainBinding
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sharedPreferencesRepositoryImpl: SharedPreferencesRepositoryImpl

    @Inject
    lateinit var usersRepositoryImpl: UsersRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        DynamicColors.applyToActivityIfAvailable(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        if (sharedPreferencesRepositoryImpl.isFirstLaunch()) {
            lifecycleScope.launch {
                val userId = UUID.randomUUID().toString()
                usersRepositoryImpl.addUser(userId, "No Name", mutableListOf())
                sharedPreferencesRepositoryImpl.setUserId(userId)
            }.invokeOnCompletion {
                sharedPreferencesRepositoryImpl.setFirstLaunch()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            binding.toolbar.setNavigationIcon(R.drawable.ic_menu)
            supportActionBar?.title = destination.label ?: getString(R.string.app_name)
        }

        binding.navView.setupWithNavController(navController)

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
