package com.lilbro.picobotella.ui.home

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Matrix
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.View as FilamentView
import com.google.android.filament.utils.ModelViewer
import com.lilbro.picobotella.R
import com.lilbro.picobotella.ui.instrucciones.InstruccionesFragment
import com.lilbro.picobotella.ui.retos.RetosActivity
import com.lilbro.picobotella.utils.ModelCache
import java.nio.ByteBuffer
import kotlin.math.ceil

class MainActivity : AppCompatActivity(),
    InstruccionesFragment.InstruccionesListener {

    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true

    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private var defaultAngle = 0f
    private val transformMatrix = FloatArray(16)

    private lateinit var bottleView: SurfaceView

    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback { nanos ->
        modelViewer.render(nanos)
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        choreographer = Choreographer.getInstance()
        setupBottle()

        val pressButton = findViewById<LottieAnimationView>(R.id.pressButton)

        pressButton.setOnClickListener {
            pressButton.isEnabled = false
            spinBottle()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        mediaPlayer?.apply {
            isLooping = true
            start()
        }

        val scaleClick = AnimationUtils.loadAnimation(this, R.anim.scale_click)
        val btnAudio = findViewById<ImageView>(R.id.btnAudio)

        findViewById<ImageView>(R.id.btnCalificar).setOnClickListener {
            it.startAnimation(scaleClick)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nequi.MobileApp"))
            startActivity(intent)
        }

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

        findViewById<ImageView>(R.id.btnRetos).setOnClickListener {
            it.startAnimation(scaleClick)
            startActivity(Intent(this, RetosActivity::class.java))
        }

        // HU 5.0 - Instrucciones: abrir como Fragment
        findViewById<ImageView>(R.id.btnInstrucciones).setOnClickListener {
            it.startAnimation(scaleClick)
            // Criterio 1: pausar audio si está ON
            if (isAudioOn) mediaPlayer?.pause()
            // Ocultar botella y botón para que no se superpongan al fragment
            bottleView.visibility = INVISIBLE
            findViewById<LottieAnimationView>(R.id.pressButton).visibility = INVISIBLE

            val fragment = InstruccionesFragment.newInstance(isAudioOn)
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment, InstruccionesFragment.TAG)
                .addToBackStack(InstruccionesFragment.TAG)
                .commit()
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

    /**
     * HU 5.0 - Criterio 3: restores background audio when instructions fragment closes.
     */
    override fun onInstruccionesClosed(restoreAudio: Boolean) {
        if (restoreAudio) {
            mediaPlayer?.start()
        }
        // Restaurar visibilidad de botella y botón al volver
        bottleView.visibility = VISIBLE
        findViewById<LottieAnimationView>(R.id.pressButton).visibility = VISIBLE
    }

    /**
     * Delegates hardware back press to InstruccionesFragment if it is visible.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(InstruccionesFragment.TAG)
        if (fragment is InstruccionesFragment && fragment.isVisible) {
            fragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupBottle() {
        bottleView = findViewById(R.id.bottleImage)
        val surfaceView = bottleView
        modelViewer = ModelViewer(surfaceView)
        val engine = modelViewer.engine
        val scene = modelViewer.scene

        surfaceView.setZOrderOnTop(true)
        surfaceView.setBackgroundColor(Color.TRANSPARENT)
        surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

        modelViewer.view.blendMode = FilamentView.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null

        val options = modelViewer.renderer.clearOptions
        options.clear = true
        modelViewer.renderer.clearOptions = options

        val mainLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(110_000f)
            .direction(0.5f, -1.0f, -1.0f)
            .castShadows(true)
            .build(engine, mainLight)
        scene.addEntity(mainLight)

        val fillLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.95f, 0.85f)
            .intensity(50_000f)
            .direction(-0.5f, 1.0f, 0.5f)
            .build(engine, fillLight)
        scene.addEntity(fillLight)

        try {
            // HERE WE USE PRELOAD MODEL
            val buffer = ModelCache.bottleBuffer ?: assets.open("models/bottle.glb").use { input ->
                val bytes = input.readBytes()
                val b = ByteBuffer.allocateDirect(bytes.size)
                b.put(bytes)
                b.flip()
                b
            }

            modelViewer.loadModelGlb(buffer)
            modelViewer.transformToUnitCube()

            modelViewer.asset?.root?.let { root ->
                val tm = engine.transformManager
                tm.getTransform(tm.getInstance(root), transformMatrix)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun spinBottle() {
        val pressButton = findViewById<LottieAnimationView>(R.id.pressButton)
        val startAngle = defaultAngle

        val direction = if (Math.random() < 0.5) 1f else -1f
        val extraRotation = 1440f + (Math.random() * 360f).toFloat()
        val finalAngle = startAngle + (direction * extraRotation)

        val spinBottleSound = MediaPlayer.create(this, R.raw.bottle_spin)
        spinBottleSound.start()

        ValueAnimator.ofFloat(startAngle, finalAngle).apply {
            duration = 5000
            interpolator = DecelerateInterpolator(1.5f)

            addUpdateListener { anim ->
                defaultAngle = anim.animatedValue as Float

                modelViewer.asset?.root?.let { root ->
                    val tm = modelViewer.engine.transformManager
                    val inst = tm.getInstance(root)

                    val rotationMatrix = FloatArray(16)
                    Matrix.setIdentityM(rotationMatrix, 0)
                    Matrix.rotateM(rotationMatrix, 0, defaultAngle, 0f, 0f, 1f)

                    val finalMatrix = FloatArray(16)
                    Matrix.multiplyMM(finalMatrix, 0, rotationMatrix, 0, transformMatrix, 0)

                    tm.setTransform(inst, finalMatrix)
                }
            }
            doOnEnd {
                pressButton.isEnabled = true
                spinBottleSound.release()
            }
        }.start()
    }

    override fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
        super.onPause()
        if (isAudioOn) mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isAudioOn) mediaPlayer?.start()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        choreographer.removeFrameCallback(frameCallback)
        if (::modelViewer.isInitialized) {
            modelViewer.destroyModel()
            modelViewer.engine.destroy()
        }
        super.onDestroy()
    }
}