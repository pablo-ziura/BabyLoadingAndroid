package com.pablo.ruiz.babyloading.feature.widget.domain.repository

import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState

interface WidgetRefreshRepository {
    fun synchronize(state: BabyProgressWidgetState)

    fun cancel()
}
