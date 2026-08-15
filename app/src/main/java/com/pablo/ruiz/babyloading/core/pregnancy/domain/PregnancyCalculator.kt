package com.pablo.ruiz.babyloading.core.pregnancy.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.GestationalAge
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyStage
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PregnancyCalculator {
    fun estimatedDueDate(lastPeriodDate: LocalDate): LocalDate {
        return lastPeriodDate.plusDays(STANDARD_PREGNANCY_DAYS.toLong())
    }

    fun gestationalAge(
        lastPeriodDate: LocalDate,
        currentDate: LocalDate,
    ): GestationalAge {
        val elapsedDays = ChronoUnit.DAYS
            .between(lastPeriodDate, currentDate)
            .coerceAtLeast(0)
            .toInt()

        return GestationalAge(
            completedWeeks = elapsedDays / DAYS_PER_WEEK,
            daysIntoWeek = elapsedDays % DAYS_PER_WEEK,
            elapsedDays = elapsedDays,
        )
    }

    fun progress(
        lastPeriodDate: LocalDate,
        currentDate: LocalDate,
    ): PregnancyProgress {
        val gestationalAge = gestationalAge(lastPeriodDate, currentDate)
        val dueDate = estimatedDueDate(lastPeriodDate)
        val daysRemaining = ChronoUnit.DAYS
            .between(currentDate, dueDate)
            .coerceAtLeast(0)
            .toInt()

        return PregnancyProgress(
            lastPeriodDate = lastPeriodDate,
            estimatedDueDate = dueDate,
            gestationalAge = gestationalAge,
            daysRemaining = daysRemaining,
            completedFraction = (gestationalAge.elapsedDays.toFloat() / STANDARD_PREGNANCY_DAYS)
                .coerceIn(0f, 1f),
            stage = stageFor(gestationalAge.completedWeeks),
        )
    }

    fun stageFor(completedWeeks: Int): PregnancyStage = when {
        completedWeeks >= REVIEW_START_WEEK -> PregnancyStage.NeedsReview
        completedWeeks >= POST_TERM_START_WEEK -> PregnancyStage.PostTerm
        completedWeeks >= ACTIVE_START_WEEK -> PregnancyStage.Active
        else -> PregnancyStage.Early
    }

    companion object {
        const val DAYS_PER_WEEK = 7
        const val STANDARD_PREGNANCY_DAYS = 280
        const val ACTIVE_START_WEEK = 6
        const val POST_TERM_START_WEEK = 41
        const val REVIEW_START_WEEK = 43
        const val LAST_JOURNEY_WEEK = 42
    }
}
