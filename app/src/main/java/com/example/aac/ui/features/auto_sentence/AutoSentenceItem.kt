package com.example.aac.ui.features.auto_sentence

import com.example.aac.ui.features.auto_sentence.repeat.RepeatSetting
import com.example.aac.ui.features.auto_sentence.time.TimeState

data class AutoSentenceItem(
    val id: Long = System.currentTimeMillis(),
    val serverId: String,
    val sentence: String,
    val repeatSetting: RepeatSetting,
    val timeState: TimeState
)
