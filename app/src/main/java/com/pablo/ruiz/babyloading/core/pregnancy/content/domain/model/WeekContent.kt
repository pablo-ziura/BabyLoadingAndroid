package com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WeekContent(
    val week: Int,
    val babySize: BabySize,
    val babySizeLabel: String,
    val milestoneTitle: String,
    val keyEvents: List<String>,
    val physiologicalImpact: String? = null,
)
