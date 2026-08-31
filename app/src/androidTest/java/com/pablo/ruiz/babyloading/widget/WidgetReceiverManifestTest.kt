package com.pablo.ruiz.babyloading.widget

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pablo.ruiz.babyloading.feature.widget.presentation.BabyProgressWidgetDailyRefreshReceiver
import com.pablo.ruiz.babyloading.feature.widget.presentation.BabyProgressWidgetReceiver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetReceiverManifestTest {
    @Test
    fun publicAndInternalReceiversHaveExpectedExportState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = context.packageManager

        val publicReceiver = packageManager.getReceiverInfo(
            ComponentName(context, BabyProgressWidgetReceiver::class.java),
            PackageManager.ComponentInfoFlags.of(0),
        )
        val internalReceiver = packageManager.getReceiverInfo(
            ComponentName(context, BabyProgressWidgetDailyRefreshReceiver::class.java),
            PackageManager.ComponentInfoFlags.of(0),
        )

        assertTrue(publicReceiver.exported)
        assertFalse(internalReceiver.exported)
    }
}
