package com.cookielauncher.app.control

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mio.datastore.GameItemBarSetting
import com.cookielauncher.app.R
import com.cookielauncher.app.databinding.DialogItembarSettingBinding
import com.cookielauncher.library.component.dialog.FCLDialog
import com.cookielauncher.library.util.ConvertUtils

class GameItemBarSettingDialog(
    context: Context,
    val setting: GameItemBarSetting,
    val callback: (GameItemBarSetting) -> Unit
) : FCLDialog(context) {
    init {
        window?.setLayout(ConvertUtils.dip2px(context, 400f), ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(R.drawable.bg_game_menu)
        val binding = DialogItembarSettingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.slideSelection.isChecked = setting.slideSelection
        binding.swapHands.isChecked = setting.doubleTapSwapHands

        binding.slideSelection.setOnCheckedChangeListener { _, isChecked ->
            callback(setting.copy(slideSelection = isChecked))
        }
        binding.swapHands.setOnCheckedChangeListener { _, isChecked ->
            callback(setting.copy(doubleTapSwapHands = isChecked))
        }
        binding.close.setOnClickListener { dismiss() }
    }
}