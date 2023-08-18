package com.app.daily.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.daily.R
import com.app.daily.data.repository.ListsRepositoryImpl
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import com.app.daily.data.repository.UsersRepositoryImpl
import com.app.daily.databinding.ActivityMainBinding
import com.app.daily.ui.fragments.ItemsFragment
import com.app.daily.ui.fragments.SettingsFragment
import com.app.daily.ui.viewmodels.MainViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
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

    @Inject
    lateinit var listsRepositoryImpl: ListsRepositoryImpl

    private val viewModel: MainViewModel by viewModels()

    private fun handleDeepLink(appLinkData: Uri) {
        val listId = appLinkData.toString().substringAfterLast("/list/")
        if (listId.isNotBlank()) {
            lifecycleScope.launch {
                val userId = sharedPreferencesRepositoryImpl.getUserId()
                if (userId!!.isNotBlank()) {
                    val user = usersRepositoryImpl.getUser(userId)
                    user?.let { currentUser ->
                        val lists = currentUser.lists.toMutableList()
                        val list = listsRepositoryImpl.getList(listId)
                        if (list != null) {
                            if(listId !in lists) {
                                lists.add(listId)
                                usersRepositoryImpl.updateUser(currentUser.copy(lists = lists))
                            }
                            val bundle = Bundle().apply {
                                putString("name", list.name)
                                putString("id", list.id)
                            }
                            val itemsFragment = ItemsFragment().apply {
                                arguments = bundle
                            }
                            val prevFragment = supportFragmentManager.findFragmentByTag(ItemsFragment.TAG)
                            if (prevFragment is ItemsFragment) {
                                prevFragment.dismiss()
                            }
                            itemsFragment.show(supportFragmentManager, ItemsFragment.TAG)
                        }
                    }
                }
            }.invokeOnCompletion {
                intent = Intent()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        //DynamicColors.applyToActivityIfAvailable(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        viewModel.themeMode.observe(this){
            when(it){
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }

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

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.nav_contact -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:decosoftapps@gmail.com"))
                    try {
                        startActivity(emailIntent)
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(binding.root, "Can't find an application to send the email", Snackbar.LENGTH_LONG).show()
                    }
                }
                R.id.nav_instagram -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/dailytodoapp?utm_source=qr&igshid=ZDc4ODBmNjlmNQ%3D%3D"))
                    try {
                        startActivity(browserIntent)
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(binding.root, "Can't find an application to open the link", Snackbar.LENGTH_LONG).show()
                    }
                }
                R.id.nav_faq -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dailytodoapp.github.io/faq.html"))
                    try {
                        startActivity(browserIntent)
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(binding.root, "Can't find an application to open the link", Snackbar.LENGTH_LONG).show()
                    }
                }
                R.id.nav_privacy -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dailytodoapp.github.io/privacypolicy.html"))
                    try {
                        startActivity(browserIntent)
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(binding.root, "Can't find an application to open the link", Snackbar.LENGTH_LONG).show()
                    }
                }
                R.id.nav_settings -> {
                    SettingsFragment().show(supportFragmentManager, SettingsFragment.TAG)
                }
            }
            binding.drawerLayout.close()
            true
        }

        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data

        appLinkData?.let {
            handleDeepLink(it)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { appLinkData ->
            handleDeepLink(appLinkData)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
