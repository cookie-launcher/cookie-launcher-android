package com.cookielauncher.app.control

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cookielauncher.app.control.data.QuickInputTexts
import com.cookielauncher.app.databinding.DialogQuickInputBinding
import com.cookielauncher.bridge.bridge.FCLBridge
import com.cookielauncher.bridge.keycodes.FCLKeycodes
import com.cookielauncher.bridge.keycodes.MinecraftKeyBindingMapper
import com.cookielauncher.library.component.dialog.FCLDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuickInputDialog(private val activity: AppCompatActivity, private val menu: GameMenu) :
    FCLDialog(activity),
    View.OnClickListener {
    private val binding: DialogQuickInputBinding

    init {
        setCancelable(false)
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        binding = DialogQuickInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addText.setOnClickListener(this)
        binding.positive.setOnClickListener(this)

        refreshList(menu)
    }

    private fun refreshList(menu: GameMenu) {
        val adapter = InputTextAdapter(
            context,
            QuickInputTexts.getInputTexts()
        ) {
            if (it.isNotEmpty()) {
                if (menu.cursorMode == FCLBridge.CursorEnabled) {
                    it.forEach { s ->
                        menu.input.sendChar(s)
                    }
                } else {
                    val gameOption = menu.gameOption
                    menu.input.sendBoundKeyEvent(
                        gameOption,
                        MinecraftKeyBindingMapper.BINDING_CHAT,
                        FCLKeycodes.KEY_T,
                        true
                    )
                    menu.input.sendBoundKeyEvent(
                        gameOption,
                        MinecraftKeyBindingMapper.BINDING_CHAT,
                        FCLKeycodes.KEY_T,
                        false
                    )
                    activity.lifecycleScope.launch {
                        delay(50)
                        it.forEach { s ->
                            menu.input.sendChar(s)
                        }
                        menu.input.sendKeyEvent(FCLKeycodes.KEY_ENTER, true)
                        menu.input.sendKeyEvent(FCLKeycodes.KEY_ENTER, false)

                    }
                }
            }

            dismiss()
        }
        binding.list.setAdapter(adapter)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.addText -> AddInputTextDialog(
                context
            ) { refreshList(menu) }.show()

            binding.positive -> dismiss()
        }
    }
}
