package com.lilbro.picobotella.ui.home

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
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

        // Iniciar audio de fondo de forma segura
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        mediaPlayer?.apply {
            isLooping = true
            start()
        }

        val scaleClick = AnimationUtils.loadAnimation(this, R.anim.scale_click)
        val btnAudio = findViewById<ImageView>(R.id.btnAudio)

        // Calificar
        findViewById<ImageView>(R.id.btnCalificar).setOnClickListener {
            it.startAnimation(scaleClick)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nequi.MobileApp"))
            startActivity(intent)
        }

        // Toggle Audio
        btnAudio.setOnClickListener {
            it.startAnimation(scaleClick)
            if (isAudioOn) {
                mediaPlayer?.pause()
                btnAudio.setImageResource(R.drawable.ic_volume_off)
            } else {
                mediaPlayer?.start()
                btnAudio.setImageResource(R.drawable.ic_volume_on)
            }
            isAudioOn = !isAudioOn
        }

        findViewById<ImageView>(R.id.btnInstrucciones).setOnClickListener {
            it.startAnimation(scaleClick)
        }

        findViewById<ImageView>(R.id.btnRetos).setOnClickListener {
            it.startAnimation(scaleClick)
            startActivity(Intent(this, RetosActivity::class.java))
        }

        findViewById<ImageView>(R.id.btnCompartir).setOnClickListener {
            it.startAnimation(scaleClick)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "¡Juega a Pico Botella!")
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir con:"))
        }
    }

    override fun onPause() {
        super.onPause()
        if (isAudioOn) mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isAudioOn) mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}