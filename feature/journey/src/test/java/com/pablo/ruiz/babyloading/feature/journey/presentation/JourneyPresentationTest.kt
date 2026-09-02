package com.pablo.ruiz.babyloading.feature.journey.presentation

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class JourneyPresentationTest {
    @Test
    fun babySizeLabelsUseTheIosCapitalization() {
        assertEquals("A Bunch Of Grapes", formatJourneyBabySizeLabel("a bunch of grapes", Locale.ENGLISH))
        assertEquals("Una Lenteja", formatJourneyBabySizeLabel("una lenteja", Locale.forLanguageTag("es-ES")))
        assertEquals("Un Arándano", formatJourneyBabySizeLabel("un arándano", Locale.forLanguageTag("es-ES")))
    }

    @Test
    fun currentDayTimelineWeekUsesTheCurrentWeekForTheFirstFourDays() {
        val state = journeyUiState(currentWeek = 20, currentDay = 3, weeks = 20..21)

        assertEquals(20, state.currentDayTimelineWeek())
    }

    @Test
    fun currentDayTimelineWeekUsesTheFollowingWeekForTheLastThreeDays() {
        val state = journeyUiState(currentWeek = 20, currentDay = 4, weeks = 20..21)

        assertEquals(21, state.currentDayTimelineWeek())
    }

    @Test
    fun currentDayTimelineWeekFallsBackWhenTheFollowingWeekIsUnavailable() {
        val state = journeyUiState(currentWeek = 40, currentDay = 6, weeks = 40..40)

        assertEquals(40, state.currentDayTimelineWeek())
    }

    @Test
    fun currentDayTimelineWeekIsUnavailableWithoutAnActiveTimelineWeek() {
        assertEquals(null, journeyUiState(currentWeek = null, currentDay = 0, weeks = 6..40).currentDayTimelineWeek())
    }

    private fun journeyUiState(
        currentWeek: Int?,
        currentDay: Int,
        weeks: IntRange,
    ) = JourneyUiState(
        isLoading = false,
        isConfigured = true,
        currentWeek = currentWeek,
        currentDay = currentDay,
        weeks = weeks.map { week ->
            JourneyWeekUiModel(
                week = week,
                status = JourneyWeekStatus.Upcoming,
                content = WeekContent(
                    week = week,
                    babySize = BabySize.Lentil,
                    babySizeLabel = "a lentil",
                    milestoneTitle = "Week $week",
                    keyEvents = listOf("Event"),
                ),
            )
        },
    )
}
