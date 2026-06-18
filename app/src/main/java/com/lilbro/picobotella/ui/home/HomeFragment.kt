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
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.View as FilamentView
import com.google.android.filament.utils.ModelViewer
import com.lilbro.picobotella.R
import com.lilbro.picobotella.data.db.AppDatabase
import com.lilbro.picobotella.data.repository.API
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.ui.instrucciones.InstruccionesFragment
import com.lilbro.picobotella.utils.ModelCache
import java.nio.ByteBuffer
import com.lilbro.picobotella.ui.instrucciones.InstruccionesActivity

class HomeFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true
    private var isFavorite = false

    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private var defaultAngle = 0f
    private val transformMatrix = FloatArray(16)

    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = PicoBotellaRepository(database.retoDao(), API.pokemonService)
        MainViewModel.Factory(repository)
    }

    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback { nanos ->
        modelViewer.render(nanos)
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choreographer = Choreographer.getInstance()
        setupBottle(view)
        setupObservers()

        val pressButton = view.findViewById<LottieAnimationView>(R.id.pressButton)
        pressButton.setOnClickListener {
            pressButton.isEnabled = false
            spinBottle(view)
        }

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.background_music)
        mediaPlayer?.apply {
            isLooping = true
            start()
        }

        val scaleClick = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_click)
        val btnAudio = view.findViewById<ImageView>(R.id.btnAudio)
        val btnCalificar = view.findViewById<ImageView>(R.id.btnCalificar)

        btnCalificar.setOnClickListener {
            it.startAnimation(scaleClick)
            isFavorite = !isFavorite
            btnCalificar.setImageResource(
                if (isFavorite) R.drawable.favorite_active else R.drawable.favorite_normal
            )
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.nequi.MobileApp")
                )
            )
        }

        btnAudio.setOnClickListener {
            it.startAnimation(scaleClick)
            if (isAudioOn) {
                mediaPlayer?.pause()
                btnAudio.setImageResource(R.drawable.volume_off)
                isAudioOn = false
            } else {
                mediaPlayer?.start()
                btnAudio.setImageResource(R.drawable.volume_up)
                isAudioOn = true
            }
        }

        view.findViewById<ImageView>(R.id.btnRetos).setOnClickListener {
            it.startAnimation(scaleClick)
            findNavController().navigate(R.id.action_home_to_retos)
        }

        view.findViewById<ImageView>(R.id.btnCompartir).setOnClickListener {
            it.startAnimation(scaleClick)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "App pico botella.\nSolo los valientes lo juegan !!\n" +
                            "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir"))
        }

        view.findViewById<ImageView>(R.id.btnInstrucciones).setOnClickListener {
            it.startAnimation(scaleClick)
            startActivity(Intent(requireContext(), InstruccionesActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.challengeEvent.observe(viewLifecycleOwner) { (imgUrl, challengeText) ->
            val fragment = ChallengeFragment.newInstance(imgUrl, challengeText)
            fragment.show(parentFragmentManager, "challenge")
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) {
            val fragment = ChallengeFragment.newInstance(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                "¡Vaya! Hubo un problema con la API"
            )
            fragment.show(parentFragmentManager, "challenge")
        }
    }

    private fun setupBottle(view: View) {
        val surfaceView = view.findViewById<SurfaceView>(R.id.bottleImage)
        val mainActivity = requireActivity() as MainActivity

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
            .color(1.0f, 1.0f, 1.0f).intensity(110_000f)
            .direction(0.5f, -1.0f, -1.0f).castShadows(true)
            .build(engine, mainLight)
        scene.addEntity(mainLight)

        val fillLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.95f, 0.85f).intensity(50_000f)
            .direction(-0.5f, 1.0f, 0.5f)
            .build(engine, fillLight)
        scene.addEntity(fillLight)

        try {
            val buf = ModelCache.bottleBuffer ?: requireContext().assets.open("models/bottle.glb").use { input ->
                val bytes = input.readBytes()
                ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); flip() }
            }
            if (ModelCache.bottleBuffer == null) ModelCache.bottleBuffer = buf
            ModelCache.bottleBuffer!!.rewind()
            modelViewer.loadModelGlb(ModelCache.bottleBuffer!!)
            modelViewer.transformToUnitCube()
        } catch (e: Exception) { e.printStackTrace() }

        mainActivity.filamentEngine = engine
        mainActivity.isModelViewerInitialized = true

        modelViewer.asset?.root?.let { root ->
            val tm = engine.transformManager
            tm.getTransform(tm.getInstance(root), transformMatrix)
        }

        choreographer.postFrameCallback(frameCallback)
    }

    private fun spinBottle(view: View) {
        val pressButton = view.findViewById<LottieAnimationView>(R.id.pressButton)
        val tvCountDown = view.findViewById<TextView>(R.id.tvCountDown)
        val startAngle = defaultAngle
        val direction = if (Math.random() < 0.5) 1f else -1f
        val extraRotation = 1440f + (Math.random() * 360f).toFloat()
        val finalAngle = startAngle + (direction * extraRotation)

        val spinBottleSound = MediaPlayer.create(requireContext(), R.raw.bottle_spin)
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
                tvCountDown.visibility = View.GONE
                spinBottleSound.release()
                viewModel.getRandomChallenge()
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
        if (isAudioOn) mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isAudioOn) mediaPlayer?.start()
        if (::modelViewer.isInitialized) {
            choreographer.postFrameCallback(frameCallback)
        }
        view?.findViewById<SurfaceView>(R.id.bottleImage)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        choreographer.removeFrameCallback(frameCallback)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroyView()
    }
}