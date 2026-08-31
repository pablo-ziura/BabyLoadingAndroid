package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import kotlinx.serialization.Serializable

@Serializable
internal data class WeekContentDto(
    val week: Int,
    val babySize: BabySizeDto,
    val babySizeLabel: String,
    val milestoneTitle: String,
    val keyEvents: List<String>,
    val physiologicalImpact: String? = null,
)
