package com.pablo.ruiz.babyloading.app.shell

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pablo.ruiz.babyloading.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTabCopyTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun tabLabelsMatchIosInSupportedLocales() {
        assertTabLabels(
            locale = Locale.ENGLISH,
            expected = listOf("Home", "My Journey", "Gallery", "Settings"),
        )
        assertTabLabels(
            locale = Locale.forLanguageTag("es-ES"),
            expected = listOf("Inicio", "Mi viaje", "Galería", "Ajustes"),
        )
    }

    private fun assertTabLabels(locale: Locale, expected: List<String>) {
        val configuration = android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        val localizedContext = context.createConfigurationContext(configuration)
        val actual = listOf(
            localizedContext.getString(R.string.dashboard_tab),
            localizedContext.getString(R.string.journey_tab),
            localizedContext.getString(R.string.gallery_tab),
            localizedContext.getString(R.string.settings_tab),
        )

        assertEquals(expected, actual)
    }
}
