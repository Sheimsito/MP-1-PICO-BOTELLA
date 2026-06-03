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
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.lilbro.picobotella.data.db.AppDatabase
import com.lilbro.picobotella.data.repository.API
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.ui.retos.RetosActivity
import com.lilbro.picobotella.utils.ModelCache
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true

    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private var defaultAngle = 0f
    private val transformMatrix = FloatArray(16)

    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = PicoBotellaRepository(database.retoDao(), API.pokemonService)
        MainViewModel.Factory(repository)
    }

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
        setupObservers()

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
        
        findViewById<ImageView>(R.id.btnCalificar).setOnClickListener {
            it.startAnimation(scaleClick)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nequi.MobileApp")))
        }

        findViewById<ImageView>(R.id.btnAudio).setOnClickListener { view ->
            view.startAnimation(scaleClick)
            val btnAudio = view as ImageView
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

        findViewById<ImageView>(R.id.btnCompartir).setOnClickListener {
            it.startAnimation(scaleClick)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "¡Juega a Pico Botella!")
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir con:"))
        }
    }

    private fun setupObservers() {
        viewModel.challengeEvent.observe(this) { (imgUrl, challengeText) ->
            val fragment = ChallengeFragment.newInstance(imgUrl, challengeText)
            fragment.show(supportFragmentManager, "challenge")
        }

        viewModel.errorEvent.observe(this) {
            val fragment = ChallengeFragment.newInstance(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                "¡Vaya! Hubo un problema con la API, pero el reto es: Realiza un RecyclerView que liste una api de Pokemones"
            )
            fragment.show(supportFragmentManager, "challenge")
        }
    }

    private fun setupBottle() {
        val surfaceView = findViewById<SurfaceView>(R.id.bottleImage)
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

        try {
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
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun spinBottle() {
        val pressButton = findViewById<LottieAnimationView>(R.id.pressButton)
        val tvCountDown = findViewById<TextView>(R.id.tvCountDown)
        val startAngle = defaultAngle
        val extraRotation = 1440f + (Math.random() * 360f).toFloat()
        val finalAngle = startAngle + extraRotation

        val durationMs = 5000L

        ValueAnimator.ofFloat(startAngle, finalAngle).apply {
            duration = durationMs
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
                tvCountDown.visibility = View.GONE
                showRandomChallenge()
            }
        }.start()
    }

    private fun showRandomChallenge() {
        viewModel.getRandomChallenge()
    }

    override fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        if (::modelViewer.isInitialized) {
            modelViewer.destroyModel()
            modelViewer.engine.destroy()
        }
        super.onDestroy()
    }
}