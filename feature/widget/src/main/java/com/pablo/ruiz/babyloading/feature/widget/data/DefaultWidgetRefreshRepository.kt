package com.pablo.ruiz.babyloading.feature.widget.data

import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.domain.repository.WidgetRefreshRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWidgetRefreshRepository @Inject constructor(
    private val alarmDataSource: WidgetRefreshAlarmDataSource,
    private val clock: Clock,
) : WidgetRefreshRepository {
    override fun synchronize(state: BabyProgressWidgetState) {
        if (state.requiresDailyRefresh) {
            alarmDataSource.schedule(WidgetDailyRefreshSchedule.nextRefreshAt(clock))
        } else {
            alarmDataSource.cancel()
        }
    }

    override fun cancel() {
        alarmDataSource.cancel()
    }
}
