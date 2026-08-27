package com.pablo.ruiz.babyloading.core.pregnancy.domain

import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.ActivePregnancyProgress
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.GestationalAge
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyPhase
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.PregnancyProgress
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
        if (lastPeriodDate.isAfter(currentDate)) {
            return PregnancyProgress.InvalidFutureLastPeriodDate(lastPeriodDate)
        }

        val gestationalAge = gestationalAge(lastPeriodDate, currentDate)
        val dueDate = estimatedDueDate(lastPeriodDate)
        return PregnancyProgress.Active(
            ActivePregnancyProgress(
            lastPeriodDate = lastPeriodDate,
            estimatedDueDate = dueDate,
            gestationalAge = gestationalAge,
            phase = phaseFor(gestationalAge),
            dueDateRelation = dueDateRelation(dueDate, currentDate),
            ),
        )
    }

    fun dueDateRelation(
        dueDate: LocalDate,
        currentDate: LocalDate,
    ): DueDateRelation {
        val dayDifference = ChronoUnit.DAYS.between(currentDate, dueDate).toInt()
        return when {
            dayDifference > 0 -> DueDateRelation.Upcoming(dayDifference)
            dayDifference == 0 -> DueDateRelation.Today
            else -> DueDateRelation.Elapsed(-dayDifference)
        }
    }

    fun phaseFor(gestationalAge: GestationalAge): PregnancyPhase = when {
        gestationalAge.elapsedDays >= POST_TERM_START_DAY -> PregnancyPhase.PostTerm
        gestationalAge.elapsedDays >= LATE_TERM_START_DAY -> PregnancyPhase.LateTerm
        else -> PregnancyPhase.Ongoing
    }

    companion object {
        const val DAYS_PER_WEEK = 7
        const val STANDARD_PREGNANCY_DAYS = 280
        const val LATE_TERM_START_DAY = 41 * DAYS_PER_WEEK
        const val POST_TERM_START_DAY = 42 * DAYS_PER_WEEK
    }
}
