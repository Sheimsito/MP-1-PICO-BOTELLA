package com.lilbro.picobotella.ui.instrucciones

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.airbnb.lottie.LottieAnimationView
import com.lilbro.picobotella.R

class InstruccionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_instrucciones)

        val toolbar = findViewById<Toolbar>(R.id.toolbarInstrucciones)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<LottieAnimationView>(R.id.lottieWinAnimation).playAnimation()
    }
}