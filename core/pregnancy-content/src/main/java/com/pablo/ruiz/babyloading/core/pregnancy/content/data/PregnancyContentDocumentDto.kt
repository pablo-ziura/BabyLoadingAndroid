package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import kotlinx.serialization.Serializable

@Serializable
internal data class PregnancyContentDocumentDto(
    val schemaVersion: Int,
    val locale: String,
    val revision: Int,
    val weeks: List<WeekContentDto>,
)
