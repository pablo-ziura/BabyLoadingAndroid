package com.pablo.ruiz.babyloading.feature.widget.presentation

import android.content.Intent
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfigFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetReceiverContractTest {
    @Test
    fun productionAndLabDailyActionsAreRecognized() {
        val factory = AppStorageConfigFactory()
        val productionAction = factory.forApplicationId(
            AppStorageConfigFactory.ProductionApplicationId,
        ).widgetDailyRefreshAction
        val labAction = factory.forApplicationId(
            AppStorageConfigFactory.LabApplicationId,
        ).widgetDailyRefreshAction

        assertTrue(WidgetDailyRefreshTrigger.matches(productionAction, productionAction))
        assertTrue(WidgetDailyRefreshTrigger.matches(labAction, labAction))
        assertFalse(WidgetDailyRefreshTrigger.matches(productionAction, labAction))
    }

    @Test
    fun clockAndBootChangesAreRecognizedWithoutAcceptingOtherBroadcasts() {
        assertTrue(WidgetSystemRefreshTrigger.matches(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(WidgetSystemRefreshTrigger.matches(Intent.ACTION_TIME_CHANGED))
        assertTrue(WidgetSystemRefreshTrigger.matches(Intent.ACTION_TIMEZONE_CHANGED))
        assertFalse(WidgetSystemRefreshTrigger.matches(Intent.ACTION_LOCALE_CHANGED))
        assertFalse(WidgetSystemRefreshTrigger.matches("com.example.DAILY_REFRESH"))
        assertFalse(WidgetSystemRefreshTrigger.matches(null))
    }

    @Test
    fun hiltEntryPointExposesOnlyWidgetOrchestrationUseCases() {
        val methodNames = BabyProgressWidgetDependencies::class.java.declaredMethods
            .map { it.name }
            .toSet()

        assertEquals(
            setOf("prepareBabyProgressWidget", "cancelBabyProgressWidgetRefresh"),
            methodNames,
        )
    }
}
