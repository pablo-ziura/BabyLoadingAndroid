package com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PregnancyContentDocument(
    val schemaVersion: Int,
    val locale: String,
    val revision: Int,
    val weeks: List<WeekContent>,
) {
    fun validated(expectedLocale: String): PregnancyContentDocument {
        if (schemaVersion != SupportedSchemaVersion) {
            throw PregnancyContentValidationException.UnsupportedSchema(schemaVersion)
        }
        if (locale != expectedLocale) {
            throw PregnancyContentValidationException.UnsupportedLocale(locale)
        }
        if (revision < 1) {
            throw PregnancyContentValidationException.InvalidRevision(revision)
        }

        val duplicateWeeks = weeks
            .groupBy(WeekContent::week)
            .filterValues { entries -> entries.size > 1 }
            .keys
            .sorted()
        if (duplicateWeeks.isNotEmpty()) {
            throw PregnancyContentValidationException.DuplicateWeeks(duplicateWeeks)
        }

        val sortedWeeks = weeks.sortedBy(WeekContent::week)
        val actualWeeks = sortedWeeks.map(WeekContent::week)
        if (actualWeeks != CoveredWeeks) {
            throw PregnancyContentValidationException.InvalidWeekCoverage(actualWeeks)
        }

        sortedWeeks.forEach { content ->
            val hasBlankText = content.babySizeLabel.isBlank() ||
                content.milestoneTitle.isBlank() ||
                content.keyEvents.isEmpty() ||
                content.keyEvents.any(String::isBlank)
            if (hasBlankText) {
                throw PregnancyContentValidationException.EmptyContent(content.week)
            }
        }

        return copy(weeks = sortedWeeks)
    }

    companion object {
        const val SupportedSchemaVersion = 1
        val CoveredWeeks: List<Int> = (6..40).toList()
    }
}

sealed class PregnancyContentValidationException(message: String) : IllegalArgumentException(message) {
    data class UnsupportedSchema(val schemaVersion: Int) :
        PregnancyContentValidationException("Unsupported schema version: $schemaVersion")

    data class UnsupportedLocale(val locale: String) :
        PregnancyContentValidationException("Unsupported locale: $locale")

    data class InvalidRevision(val revision: Int) :
        PregnancyContentValidationException("Invalid revision: $revision")

    data class DuplicateWeeks(val weeks: List<Int>) :
        PregnancyContentValidationException("Duplicate weeks: $weeks")

    data class InvalidWeekCoverage(val weeks: List<Int>) :
        PregnancyContentValidationException("Invalid week coverage: $weeks")

    data class EmptyContent(val week: Int) :
        PregnancyContentValidationException("Empty content for week: $week")
}
