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
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import com.lilbro.picobotella.R
import com.lilbro.picobotella.ui.retos.RetosActivity
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true

    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private var defaultAngle = 0f
    private val transformMatrix = FloatArray(16) // Matriz para guardar la escala original

    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback { nanos ->
        modelViewer.render(nanos)
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init Filament
        Utils.init()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        choreographer = Choreographer.getInstance()
        setupBottle()

        findViewById<LottieAnimationView>(R.id.pressButton).setOnClickListener {
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

        findViewById<ImageView>(R.id.btnCompartir).setOnClickListener {
            it.startAnimation(scaleClick)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "¡Juega a Pico Botella!")
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir con:"))
        }
    }

    // Setup Bottle (3D Model)
    private fun setupBottle() {
        val surfaceView = findViewById<SurfaceView>(R.id.bottleImage)

        modelViewer = ModelViewer(surfaceView)
        val engine = modelViewer.engine
        val scene = modelViewer.scene

        modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT

        val options = modelViewer.renderer.clearOptions
        options.clear = true
        options.clearColor = floatArrayOf(
            0.85f,
            0.70f,
            0.57f,
            1f
        )
        modelViewer.renderer.clearOptions = options

        // Lights and skybox
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
            // Here we load the 3D model from the assets folder
            assets.open("models/bottle.glb").use { input ->
                val buffer = ByteBuffer.wrap(input.readBytes())
                modelViewer.loadModelGlb(buffer)
                modelViewer.transformToUnitCube()

                // Save the original scale of the model
                modelViewer.asset?.root?.let { root ->
                    val tm = engine.transformManager
                    tm.getTransform(tm.getInstance(root), transformMatrix)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun spinBottle() {
        val startAngle = defaultAngle
        val finalAngle = startAngle + 1440f + (Math.random() * 360f).toFloat()

        ValueAnimator.ofFloat(startAngle, finalAngle).apply {
            duration = 4000
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { anim ->
                defaultAngle = anim.animatedValue as Float
                modelViewer.asset?.root?.let { root ->
                    val tm = modelViewer.engine.transformManager
                    val inst = tm.getInstance(root)

                    val rotationMatrix = FloatArray(16)
                    Matrix.setIdentityM(rotationMatrix, 0)
                    // Giro tipo reloj (Eje Z)
                    Matrix.rotateM(rotationMatrix, 0, defaultAngle, 0f, 0f, 1f)

                    val finalMatrix = FloatArray(16)
                    Matrix.multiplyMM(finalMatrix, 0, rotationMatrix, 0, transformMatrix, 0)

                    tm.setTransform(inst, finalMatrix)
                }
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
