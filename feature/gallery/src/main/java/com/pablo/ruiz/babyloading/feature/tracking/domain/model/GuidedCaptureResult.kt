package com.pablo.ruiz.babyloading.feature.tracking.domain.model

import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem

data class GuidedCaptureResult(
    val galleryItem: GalleryItem,
    val publicCopySaved: Boolean,
)
