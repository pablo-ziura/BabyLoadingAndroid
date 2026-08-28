package com.pablo.ruiz.babyloading.feature.widget.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pablo.ruiz.babyloading.core.storage.AppStorageNames
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.presentation.BabyProgressWidgetReceiver
import java.time.Clock

internal class WidgetDailyRefreshScheduler(
    context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val applicationContext = context.applicationContext
    private val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)

    fun scheduleNextRefresh() {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            WidgetDailyRefreshSchedule.nextRefreshAt(clock).toEpochMilli(),
            refreshPendingIntent(),
        )
    }

    fun cancel() {
        alarmManager.cancel(refreshPendingIntent())
    }

    fun scheduleFor(state: BabyProgressWidgetState) {
        if (state.requiresDailyRefresh) {
            scheduleNextRefresh()
        } else {
            cancel()
        }
    }

    private fun refreshPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            applicationContext,
            DAILY_REFRESH_REQUEST_CODE,
            Intent(applicationContext, BabyProgressWidgetReceiver::class.java)
                .setAction(DAILY_REFRESH_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        val DAILY_REFRESH_ACTION: String
            get() = AppStorageNames.current.widgetDailyRefreshAction

        private const val DAILY_REFRESH_REQUEST_CODE = 1
    }
}
