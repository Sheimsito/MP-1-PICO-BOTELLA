package com.lilbro.picobotella.ui.home

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.lilbro.picobotella.R
import com.lilbro.picobotella.ui.instrucciones.InstruccionesActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var isAudioOn = true
    private var isFavorite = false

    @Inject
    lateinit var bottleRenderer: BottleRenderer

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(bottleRenderer)
        bottleRenderer.setupBottle(view.findViewById(R.id.bottleImage), requireContext())
        
        setupObservers()

        val pressButton = view.findViewById<LottieAnimationView>(R.id.pressButton)
        pressButton.setOnClickListener {
            pressButton.isEnabled = false
            val spinBottleSound = MediaPlayer.create(requireContext(), R.raw.bottle_spin)
            spinBottleSound.start()
            
            bottleRenderer.spinBottle {
                pressButton.isEnabled = true
                spinBottleSound.release()
                viewModel.getRandomChallenge()
            }
        }

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.background_music_trabajepapi)
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
                    "https://play.google.com/store/apps/details?id=com.nequi.MobileApp".toUri()
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
                    "App pico botella.\\nSolo los valientes lo juegan !!\\n" +
                            "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir"))
        }

        val btnLogout = view.findViewById<ImageView>(R.id.btnLogout)

        btnLogout.setOnClickListener {  
            it.startAnimation(scaleClick)
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_home_to_login)
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

    override fun onPause() {
        super.onPause()
        if (isAudioOn) mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isAudioOn) mediaPlayer?.start()
    }

    override fun onDestroyView() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroyView()
    }
}
