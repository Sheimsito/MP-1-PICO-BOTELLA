package com.lilbro.picobotella.ui.instrucciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.lilbro.picobotella.R

/**
 * Fragment that displays the game instructions for Pico Botella.
 *
 * Satisfies HU 5.0 acceptance criteria:
 * - Dark gray background covering the full screen
 * - Custom toolbar with title "Reglas del Juego" and back arrow
 * - Back arrow pops this fragment and notifies [InstruccionesListener] to restore audio
 * - Game rules with bold white titles and white descriptions
 * - Lottie win/trophy animation
 *
 * The host Activity must implement [InstruccionesListener] to handle audio restoration.
 */
class InstruccionesFragment : Fragment() {

    /**
     * Callback interface implemented by the host Activity.
     *
     * Called when the user navigates back from the instructions screen
     * so the host can restore background audio if it was active.
     */
    interface InstruccionesListener {
        /**
         * Invoked when the instructions screen is closed.
         *
         * @param restoreAudio True if background audio should be resumed.
         */
        fun onInstruccionesClosed(restoreAudio: Boolean)
    }

    private var wasAudioOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wasAudioOn = arguments?.getBoolean(ARG_AUDIO_ON, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_instrucciones, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(view)
        setupAnimation(view)
    }

    /**
     * Configures the toolbar navigation back arrow.
     *
     * @param view The fragment's root view.
     */
    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbarInstrucciones)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }
    }

    /**
     * Starts the Lottie win animation.
     *
     * @param view The fragment's root view.
     */
    private fun setupAnimation(view: View) {
        view.findViewById<LottieAnimationView>(R.id.lottieWinAnimation).playAnimation()
    }

    /**
     * Pops this fragment from the back stack and notifies the host Activity
     * to restore audio if it was playing before.
     */
    private fun closeFragment() {
        (activity as? InstruccionesListener)?.onInstruccionesClosed(wasAudioOn)
        parentFragmentManager.popBackStack()
    }

    /**
     * Intercepts the hardware back button to mirror the toolbar back arrow behavior.
     */
    fun onBackPressed(): Boolean {
        closeFragment()
        return true
    }

    companion object {
        /** Fragment tag used when adding to the back stack. */
        const val TAG = "InstruccionesFragment"

        private const val ARG_AUDIO_ON = "arg_audio_on"

        /**
         * Creates a new instance of [InstruccionesFragment].
         *
         * @param audioOn Whether background audio was active when the fragment was launched.
         * @return A configured [InstruccionesFragment] instance.
         */
        fun newInstance(audioOn: Boolean): InstruccionesFragment {
            return InstruccionesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_AUDIO_ON, audioOn)
                }
            }
        }
    }
}