package com.pablo.ruiz.babyloading.feature.tracking.domain.model

data class CapturedPhotoFile(
    val temporaryFilePath: String,
    val sizeBytes: Long,
) {
    init {
        require(temporaryFilePath.isNotBlank()) { "Captured photo path cannot be blank" }
        require(sizeBytes > 0) { "Captured photo cannot be empty" }
    }
}
