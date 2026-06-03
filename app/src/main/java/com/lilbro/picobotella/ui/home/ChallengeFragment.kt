package com.lilbro.picobotella.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import coil.load
import com.lilbro.picobotella.databinding.FragmentChallengeBinding

class ChallengeFragment : DialogFragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_POKEMON_IMG = "pokemon_img"
        private const val ARG_CHALLENGE = "challenge_text"

        fun newInstance(imageUrl: String, challenge: String): ChallengeFragment {
            val fragment = ChallengeFragment()
            val args = Bundle()
            args.putString(ARG_POKEMON_IMG, imageUrl)
            args.putString(ARG_CHALLENGE, challenge)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dialog cannot be dismissed by clicking outside
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imageUrl = arguments?.getString(ARG_POKEMON_IMG)
        val challengeText = arguments?.getString(ARG_CHALLENGE)


        binding.ivPokemon.load(imageUrl)
        

        binding.tvChallenge.text = challengeText


        binding.btnCerrar.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}