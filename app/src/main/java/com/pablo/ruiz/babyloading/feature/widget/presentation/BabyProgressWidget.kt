package com.pablo.ruiz.babyloading.feature.widget.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pablo.ruiz.babyloading.MainActivity
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetStateFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.flow.first

class BabyProgressWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dependencies = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BabyProgressWidgetDependencies::class.java,
        )
        val state = runCatching {
            BabyProgressWidgetStateFactory(dependencies.pregnancyCalculator()).create(
                lastPeriodDate = dependencies.pregnancyRepository().lastPeriodDate.first(),
                currentDate = LocalDate.now(dependencies.clock()),
            )
        }.getOrDefault(BabyProgressWidgetState.NeedsSetup)
        val strings = BabyProgressWidgetStrings.create(context, state)

        provideContent {
            BabyProgressWidgetContent(
                state = state,
                strings = strings,
            )
        }
    }
}

class BabyProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BabyProgressWidget()
}

@Composable
private fun BabyProgressWidgetContent(
    state: BabyProgressWidgetState,
    strings: BabyProgressWidgetStrings,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetSurface)
            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            .clickable(actionStartActivity<MainActivity>())
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = strings.title,
            style = TextStyle(
                color = WidgetPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.height(6.dp))
        when (state) {
            BabyProgressWidgetState.NeedsSetup -> NeedsSetupContent(strings)
            is BabyProgressWidgetState.Progress -> ProgressContent(state, strings)
        }
    }
}

@Composable
private fun NeedsSetupContent(strings: BabyProgressWidgetStrings) {
    Text(
        text = strings.setupTitle,
        style = TextStyle(
            color = WidgetOnSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
    Spacer(GlanceModifier.height(8.dp))
    Text(
        text = strings.setupMessage,
        style = TextStyle(color = WidgetOnSurface, fontSize = 14.sp),
    )
}

@Composable
private fun ProgressContent(
    state: BabyProgressWidgetState.Progress,
    strings: BabyProgressWidgetStrings,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = strings.week,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = WidgetOnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = strings.daysRemaining,
            style = TextStyle(
                color = WidgetOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
    Spacer(GlanceModifier.height(12.dp))
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(8.dp)
            .background(WidgetTrack)
            .cornerRadius(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = GlanceModifier
                .width((WIDGET_PROGRESS_WIDTH_DP * state.completedFraction).coerceAtLeast(4f).dp)
                .height(8.dp)
                .background(WidgetPrimary)
                .cornerRadius(4.dp),
        ) {}
    }
    Spacer(GlanceModifier.height(10.dp))
    Text(
        text = strings.dueDate,
        style = TextStyle(color = WidgetOnSurfaceMuted, fontSize = 13.sp),
    )
}

private data class BabyProgressWidgetStrings(
    val title: String,
    val setupTitle: String,
    val setupMessage: String,
    val week: String,
    val daysRemaining: String,
    val dueDate: String,
) {
    companion object {
        fun create(
            context: Context,
            state: BabyProgressWidgetState,
        ): BabyProgressWidgetStrings {
            if (state !is BabyProgressWidgetState.Progress) {
                return BabyProgressWidgetStrings(
                    title = context.getString(R.string.widget_title),
                    setupTitle = context.getString(R.string.widget_setup_title),
                    setupMessage = context.getString(R.string.widget_setup_message),
                    week = "",
                    daysRemaining = "",
                    dueDate = "",
                )
            }
            val locale = context.resources.configuration.locales[0]
            val formattedDueDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .format(state.estimatedDueDate)
            return BabyProgressWidgetStrings(
                title = context.getString(R.string.widget_title),
                setupTitle = "",
                setupMessage = "",
                week = context.getString(
                    R.string.widget_week_and_day,
                    state.completedWeeks,
                    state.daysIntoWeek,
                ),
                daysRemaining = context.resources.getQuantityString(
                    R.plurals.widget_days_remaining,
                    state.daysRemaining,
                    state.daysRemaining,
                ),
                dueDate = context.getString(R.string.widget_due_date, formattedDueDate),
            )
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BabyProgressWidgetDependencies {
    fun pregnancyRepository(): PregnancyRepository

    fun pregnancyCalculator(): PregnancyCalculator

    fun clock(): Clock
}

private val WidgetSurface = ColorProvider(Color(0xFFFFF8F5))
private val WidgetPrimary = ColorProvider(Color(0xFF96516A))
private val WidgetOnSurface = ColorProvider(Color(0xFF29171D))
private val WidgetOnSurfaceMuted = ColorProvider(Color(0xFF68585D))
private val WidgetTrack = ColorProvider(Color(0xFFF0DCE3))
private const val WIDGET_PROGRESS_WIDTH_DP = 214f
