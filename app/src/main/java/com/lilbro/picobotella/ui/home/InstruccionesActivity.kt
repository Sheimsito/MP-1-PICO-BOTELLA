package com.lilbro.picobotella.ui.instrucciones

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lilbro.picobotella.R

class InstruccionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_instrucciones)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarInstrucciones)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottieWinAnimation).playAnimation()
    }
}