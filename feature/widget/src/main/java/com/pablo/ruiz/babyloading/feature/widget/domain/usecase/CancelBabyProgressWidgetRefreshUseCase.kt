package com.pablo.ruiz.babyloading.feature.widget.domain.usecase

import com.pablo.ruiz.babyloading.feature.widget.domain.repository.WidgetRefreshRepository
import javax.inject.Inject

class CancelBabyProgressWidgetRefreshUseCase @Inject constructor(
    private val refreshRepository: WidgetRefreshRepository,
) {
    operator fun invoke() {
        refreshRepository.cancel()
    }
}
