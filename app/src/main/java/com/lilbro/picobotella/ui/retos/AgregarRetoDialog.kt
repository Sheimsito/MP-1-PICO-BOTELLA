package com.lilbro.picobotella.ui.retos

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lilbro.picobotella.R

/**
 * Dialog fragment for adding a new challenge (reto).
 *
 * Satisfies US 7.0 acceptance criteria:
 * - Input field inside a dialog
 * - Save button disabled when input is empty
 * - Calls [onGuardar] with the trimmed text on confirmation
 *
 * @property onGuardar Callback invoked with the trimmed description when the user confirms.
 */
class AgregarRetoDialog(
    private val onGuardar: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val layout = TextInputLayout(context).apply {
            hint = getString(R.string.hint_descripcion_reto)
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dialog_padding),
                resources.getDimensionPixelSize(R.dimen.dialog_padding_top),
                resources.getDimensionPixelSize(R.dimen.dialog_padding),
                0
            )
        }

        val input = TextInputEditText(context)
        layout.addView(input)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_agregar_reto)
            .setView(layout)
            .setPositiveButton(R.string.btn_guardar, null)
            .setNegativeButton(R.string.btn_cancelar, null)
            .create()

        dialog.setOnShowListener {
            val btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            // Save button starts disabled until the user types something
            btnGuardar.isEnabled = false

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    btnGuardar.isEnabled = s?.toString()?.trim()?.isNotEmpty() == true
                }
            })

            btnGuardar.setOnClickListener {
                val texto = input.text?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    onGuardar(texto)
                    dismiss()
                }
            }
        }

        return dialog
    }

    companion object {
        /** Tag used when showing this dialog via FragmentManager. */
        const val TAG = "AgregarRetoDialog"
    }
}