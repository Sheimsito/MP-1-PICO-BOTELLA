package com.lilbro.picobotella.ui.home

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.lilbro.picobotella.R
import com.lilbro.picobotella.ui.retos.RetosActivity

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Iniciar audio de fondo
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music).apply {
            isLooping = true
            start()
        }

        val scaleClick = AnimationUtils.loadAnimation(this, R.anim.scale_click)
        val btnAudio = findViewById<ImageButton>(R.id.btnAudio)

        // Calificar → Google Play (HU 4)
        findViewById<ImageButton>(R.id.btnCalificar).setOnClickListener {
            it.startAnimation(scaleClick)
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es")))
        }

        // Toggle audio ON/OFF (HU 3 - Criterio 3)
        btnAudio.setOnClickListener {
            it.startAnimation(scaleClick)
            if (isAudioOn) {
                mediaPlayer?.pause()
                btnAudio.setImageResource(R.drawable.ic_volume_off)
                isAudioOn = false
            } else {
                mediaPlayer?.start()
                btnAudio.setImageResource(R.drawable.ic_volume_on)
                isAudioOn = true
            }
        }

        // Instrucciones (HU 5) — pendiente
        findViewById<ImageButton>(R.id.btnInstrucciones).setOnClickListener {
            it.startAnimation(scaleClick)
            // startActivity(Intent(this, InstruccionesActivity::class.java))
        }

        // Retos (HU 6)
        findViewById<ImageButton>(R.id.btnRetos).setOnClickListener {
            it.startAnimation(scaleClick)
            startActivity(Intent(this, RetosActivity::class.java))
        }

        // Compartir (HU 10)
        findViewById<ImageButton>(R.id.btnCompartir).setOnClickListener {
            it.startAnimation(scaleClick)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    "App pico botella.\nSolo los valientes lo juegan !!\n" +
                            "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es")
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir"))
        }

        findViewById<MaterialButton>(R.id.btnIrARetos).setOnClickListener {
            startActivity(Intent(this, RetosActivity::class.java))
        }
    }

    // Pausar audio cuando la app va al fondo
    override fun onPause() {
        super.onPause()
        if (isAudioOn) mediaPlayer?.pause()
    }

    // Reanudar audio cuando la app vuelve
    override fun onResume() {
        super.onResume()
        if (isAudioOn) mediaPlayer?.start()
    }

    // Liberar recursos cuando se destruye la activity
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}