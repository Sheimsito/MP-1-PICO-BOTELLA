package com.lilbro.picobotella.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.Engine
import com.lilbro.picobotella.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    var isModelViewerInitialized = false
    lateinit var filamentEngine: Engine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        if (isFinishing && isModelViewerInitialized) {
            filamentEngine.destroy()
        }
        super.onDestroy()
    }
}
