package com.lilbro.picobotella.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.Utils
import com.lilbro.picobotella.R
import com.lilbro.picobotella.ui.home.MainActivity
import com.lilbro.picobotella.utils.ModelCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Initialize Filament as soon as possible
        Utils.init()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            // Preload the 3D model in background
            ModelCache.preloadModel(this@SplashScreen)
            
            // Wait remaining time if needed (total 3 seconds instead of 5 to make it feel faster)
            delay(3000)
            
            val intent = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}