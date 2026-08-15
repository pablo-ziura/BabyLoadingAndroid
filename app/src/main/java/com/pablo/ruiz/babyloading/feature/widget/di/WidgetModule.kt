package com.pablo.ruiz.babyloading.feature.widget.di

import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.feature.widget.data.GlancePregnancyDataChangeNotifier
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
}
