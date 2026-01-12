package org.example.project.domain.model

import kotlinx.datetime.LocalDate

data class PsychologyNote(
    val id: String,
    val date: LocalDate,

    val beforeTradeEmotion: Emotion,
    val duringTradeEmotion: Emotion,
    val afterTradeEmotion: Emotion,

    val confidenceLevel: Int, // 1–10
    val note: String
)
