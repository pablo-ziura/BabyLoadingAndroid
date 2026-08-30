package com.pablo.ruiz.babyloading.feature.widget.di

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.feature.widget.data.AlarmManagerWidgetRefreshAlarmDataSource
import com.pablo.ruiz.babyloading.feature.widget.data.DefaultWidgetRefreshRepository
import com.pablo.ruiz.babyloading.feature.widget.data.GlancePregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.feature.widget.data.WidgetRefreshAlarmDataSource
import com.pablo.ruiz.babyloading.feature.widget.domain.repository.WidgetRefreshRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    @Singleton
    abstract fun bindPregnancyDataChangeNotifier(
        notifier: GlancePregnancyDataChangeNotifier,
    ): PregnancyDataChangeNotifier

    @Binds
    @Singleton
    abstract fun bindWidgetRefreshAlarmDataSource(
        dataSource: AlarmManagerWidgetRefreshAlarmDataSource,
    ): WidgetRefreshAlarmDataSource

    @Binds
    @Singleton
    abstract fun bindWidgetRefreshRepository(
        repository: DefaultWidgetRefreshRepository,
    ): WidgetRefreshRepository
}
