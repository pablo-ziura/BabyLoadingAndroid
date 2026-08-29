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

        assertTrue(WidgetRefreshTrigger.matches(productionAction, productionAction))
        assertTrue(WidgetRefreshTrigger.matches(labAction, labAction))
        assertFalse(WidgetRefreshTrigger.matches(productionAction, labAction))
    }

    @Test
    fun clockAndBootChangesAreRecognizedWithoutAcceptingOtherBroadcasts() {
        val dailyAction = "com.example.DAILY_REFRESH"

        assertTrue(WidgetRefreshTrigger.matches(Intent.ACTION_BOOT_COMPLETED, dailyAction))
        assertTrue(WidgetRefreshTrigger.matches(Intent.ACTION_TIME_CHANGED, dailyAction))
        assertTrue(WidgetRefreshTrigger.matches(Intent.ACTION_TIMEZONE_CHANGED, dailyAction))
        assertFalse(WidgetRefreshTrigger.matches(Intent.ACTION_LOCALE_CHANGED, dailyAction))
        assertFalse(WidgetRefreshTrigger.matches(null, dailyAction))
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
