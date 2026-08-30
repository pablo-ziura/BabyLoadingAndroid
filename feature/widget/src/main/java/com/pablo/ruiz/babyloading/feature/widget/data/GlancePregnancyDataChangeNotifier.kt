package com.pablo.ruiz.babyloading.feature.widget.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyDataChangeNotifier
import com.pablo.ruiz.babyloading.feature.widget.presentation.BabyProgressWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlancePregnancyDataChangeNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PregnancyDataChangeNotifier {
    override suspend fun onPregnancyDataChanged() {
        BabyProgressWidget().updateAll(context)
    }
}
