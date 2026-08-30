package com.pablo.ruiz.babyloading.feature.gallery.domain.usecase

import com.pablo.ruiz.babyloading.feature.gallery.domain.repository.GalleryRepository
import javax.inject.Inject

class DeletePrivateGalleryCopyUseCase @Inject constructor(
    private val repository: GalleryRepository,
) {
    suspend operator fun invoke(id: String) {
        repository.deletePrivateCopy(id)
    }
}
