package com.pablo.ruiz.babyloading.feature.widget.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.GestationalAge

sealed interface BabyProgressWidgetState {
    data object NeedsSetup : BabyProgressWidgetState
    data object InvalidFutureLastPeriodDate : BabyProgressWidgetState

    data class Ongoing(val progress: BabyProgressWidgetDetails) : BabyProgressWidgetState
    data class LateTerm(val progress: BabyProgressWidgetDetails) : BabyProgressWidgetState
    data class PostTerm(val progress: BabyProgressWidgetDetails) : BabyProgressWidgetState

    val requiresDailyRefresh: Boolean
        get() = this !is NeedsSetup && this !is InvalidFutureLastPeriodDate
}

data class BabyProgressWidgetDetails(
    val gestationalAge: GestationalAge,
    val dueDateRelation: DueDateRelation,
)
