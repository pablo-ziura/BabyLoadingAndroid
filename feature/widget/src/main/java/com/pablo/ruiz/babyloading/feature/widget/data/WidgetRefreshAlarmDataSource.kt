package com.pablo.ruiz.babyloading.feature.widget.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pablo.ruiz.babyloading.core.storage.AppStorageConfig
import com.pablo.ruiz.babyloading.feature.widget.presentation.BabyProgressWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface WidgetRefreshAlarmDataSource {
    fun schedule(refreshAt: Instant)

    fun cancel()
}

@Singleton
class AlarmManagerWidgetRefreshAlarmDataSource @Inject constructor(
    @ApplicationContext context: Context,
    private val storageConfig: AppStorageConfig,
) : WidgetRefreshAlarmDataSource {
    private val applicationContext = context.applicationContext
    private val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)

    override fun schedule(refreshAt: Instant) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            refreshAt.toEpochMilli(),
            refreshPendingIntent(),
        )
    }

    override fun cancel() {
        alarmManager.cancel(refreshPendingIntent())
    }

    private fun refreshPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            applicationContext,
            DAILY_REFRESH_REQUEST_CODE,
            Intent(applicationContext, BabyProgressWidgetReceiver::class.java)
                .setAction(storageConfig.widgetDailyRefreshAction),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val DAILY_REFRESH_REQUEST_CODE = 1
    }
}
