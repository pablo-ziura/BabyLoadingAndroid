package com.pablo.ruiz.babyloading.feature.dashboard.presentation

import android.content.Context
import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pablo.ruiz.babyloading.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardCopyTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun babySizeCopyMatchesIosInSupportedLocales() {
        assertEquals(
            "Your baby is now the size of a bunch of grapes",
            localizedBabySizeCopy(
                locale = Locale.US,
                babySize = "a bunch of grapes",
            ),
        )
        assertEquals(
            "Tu bebé ahora es del tamaño de un racimo de uvas",
            localizedBabySizeCopy(
                locale = Locale.forLanguageTag("es-ES"),
                babySize = "un racimo de uvas",
            ),
        )
    }

    @Test
    fun statisticsCopyMatchesIosInSupportedLocales() {
        assertEquals("Week", localizedString(Locale.US, R.string.dashboard_week))
        assertEquals(
            "Days until estimated due date",
            localizedString(Locale.US, R.string.dashboard_days_until_due_date),
        )
        assertEquals(
            "Estimated due date",
            localizedString(Locale.US, R.string.dashboard_due_date_metric),
        )
        assertEquals(
            "Days since estimated due date",
            localizedString(Locale.US, R.string.dashboard_days_since_due_date),
        )
        assertEquals(
            "Estimated due date",
            localizedString(Locale.US, R.string.dashboard_due_date),
        )

        val spanishLocale = Locale.forLanguageTag("es-ES")
        assertEquals("Semana", localizedString(spanishLocale, R.string.dashboard_week))
        assertEquals(
            "Días hasta la fecha estimada de parto",
            localizedString(spanishLocale, R.string.dashboard_days_until_due_date),
        )
        assertEquals(
            "Fecha estimada de parto",
            localizedString(spanishLocale, R.string.dashboard_due_date_metric),
        )
        assertEquals(
            "Días desde la fecha estimada de parto",
            localizedString(spanishLocale, R.string.dashboard_days_since_due_date),
        )
        assertEquals(
            "Fecha probable de parto",
            localizedString(spanishLocale, R.string.dashboard_due_date),
        )
    }

    private fun localizedBabySizeCopy(
        locale: Locale,
        babySize: String,
    ): String = localizedContext(locale).getString(R.string.dashboard_baby_size, babySize)

    private fun localizedString(
        locale: Locale,
        resourceId: Int,
    ): String = localizedContext(locale).getString(resourceId)

    private fun localizedContext(locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(configuration)
    }
}
