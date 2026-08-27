package com.pablo.ruiz.babyloading.feature.widget.presentation

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pablo.ruiz.babyloading.MainActivity
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.localization.AppLanguageProvider
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.repository.PregnancyContentRepository
import com.pablo.ruiz.babyloading.core.pregnancy.content.presentation.drawableResource
import com.pablo.ruiz.babyloading.core.pregnancy.domain.PregnancyCalculator
import com.pablo.ruiz.babyloading.core.pregnancy.domain.model.DueDateRelation
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetStateFactory
import com.pablo.ruiz.babyloading.feature.widget.data.WidgetDailyRefreshScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip

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
        WidgetDailyRefreshScheduler(context).scheduleFor(state)
        val weekContent = (state as? BabyProgressWidgetState.Ongoing)?.let { ongoing ->
            runCatching {
                dependencies.pregnancyContentRepository().contentForWeek(
                    week = ongoing.progress.gestationalAge.completedWeeks,
                    language = dependencies.appLanguageProvider().currentLanguage(),
                )
            }.getOrNull()
        }
        val strings = BabyProgressWidgetStrings.create(context, state, weekContent)
        val babySizeProgressImage = (state as? BabyProgressWidgetState.Ongoing)
            ?.let { ongoing ->
                weekContent?.let { content ->
                    ImageProvider(
                        BabySizeProgressImageRenderer.create(
                            context = context,
                            drawableRes = content.babySize.drawableResource(),
                            progress = ongoing.progress.completionFraction(),
                        ),
                    )
                }
            }

        provideContent {
            BabyProgressWidgetContent(
                state = state,
                strings = strings,
                babySizeProgressImage = babySizeProgressImage,
            )
        }
    }
}

class BabyProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BabyProgressWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        WidgetDailyRefreshScheduler(context).cancel()
        super.onDisabled(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WidgetDailyRefreshScheduler.DAILY_REFRESH_ACTION,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            -> {
                if (hasWidgets(context)) {
                    refreshWidgets(context)
                }
            }

            else -> super.onReceive(context, intent)
        }
    }

    private fun hasWidgets(context: Context): Boolean {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, BabyProgressWidgetReceiver::class.java),
        ).isNotEmpty()
    }

    private fun refreshWidgets(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                runCatching { BabyProgressWidget().updateAll(context.applicationContext) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

@Composable
private fun BabyProgressWidgetContent(
    state: BabyProgressWidgetState,
    strings: BabyProgressWidgetStrings,
    babySizeProgressImage: ImageProvider?,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_progress_background))
            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            BabyProgressWidgetState.NeedsSetup -> NeedsSetupContent(strings)
            BabyProgressWidgetState.InvalidFutureLastPeriodDate -> InvalidDateContent(strings)
            is BabyProgressWidgetState.Ongoing -> OngoingContent(
                state = state,
                strings = strings,
                babySizeProgressImage = babySizeProgressImage,
            )
            is BabyProgressWidgetState.LateTerm -> PhaseContent(
                strings = strings,
            )
            is BabyProgressWidgetState.PostTerm -> PhaseContent(
                strings = strings,
            )
        }
    }
}

@Composable
private fun NeedsSetupContent(strings: BabyProgressWidgetStrings) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_calendar),
            contentDescription = null,
            modifier = GlanceModifier.size(36.dp),
        )
        Spacer(GlanceModifier.width(12.dp))
        Text(
            text = strings.setupMessage,
            style = TextStyle(
                color = WidgetContentMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun OngoingContent(
    state: BabyProgressWidgetState.Ongoing,
    strings: BabyProgressWidgetStrings,
    babySizeProgressImage: ImageProvider?,
) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (babySizeProgressImage != null) {
            Image(
                provider = babySizeProgressImage,
                contentDescription = strings.babySizeContentDescription,
                modifier = GlanceModifier.size(WIDGET_RING_SIZE_DP.dp),
            )
        } else {
            Box(
                modifier = GlanceModifier
                    .size(WIDGET_RING_SIZE_DP.dp)
                    .background(WidgetRingSurface)
                    .cornerRadius((WIDGET_RING_SIZE_DP / 2).dp),
            ) {}
        }
        Spacer(GlanceModifier.width(12.dp))
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
        ) {
            Text(
                text = strings.week,
                style = TextStyle(
                    color = WidgetContent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = strings.babySize,
                style = TextStyle(
                    color = WidgetContentMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 3,
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = strings.dueDateRelation,
                style = TextStyle(
                    color = WidgetContent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 2,
            )
            Spacer(GlanceModifier.height(4.dp))
            LinearProgressIndicator(
                progress = state.progress.completionFraction(),
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = WidgetContent,
                backgroundColor = WidgetProgressTrack,
            )
        }
    }
}

@Composable
private fun InvalidDateContent(strings: BabyProgressWidgetStrings) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_calendar),
            contentDescription = null,
            modifier = GlanceModifier.size(36.dp),
        )
        Spacer(GlanceModifier.width(12.dp))
        Text(
            text = strings.invalidDateMessage,
            style = TextStyle(
                color = WidgetContentMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 3,
        )
    }
}

@Composable
private fun PhaseContent(
    strings: BabyProgressWidgetStrings,
) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_calendar_alert),
            contentDescription = null,
            modifier = GlanceModifier.size(32.dp),
        )
        Spacer(GlanceModifier.width(12.dp))
        Column(modifier = GlanceModifier.width(PHASE_TEXT_WIDTH_DP.dp)) {
            Text(
                text = strings.phaseTitle,
                style = TextStyle(
                    color = WidgetContent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = strings.gestationalAge,
                style = TextStyle(
                    color = WidgetContent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = strings.dueDateRelation,
                style = TextStyle(
                    color = WidgetContentMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 2,
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = strings.reviewDateMessage,
                style = TextStyle(
                    color = WidgetContentMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 2,
            )
        }
    }
}

private data class BabyProgressWidgetStrings(
    val setupMessage: String,
    val invalidDateMessage: String,
    val week: String,
    val babySize: String,
    val babySizeContentDescription: String?,
    val dueDateRelation: String,
    val gestationalAge: String,
    val phaseTitle: String,
    val reviewDateMessage: String,
) {
    companion object {
        fun create(
            context: Context,
            state: BabyProgressWidgetState,
            weekContent: WeekContent?,
        ): BabyProgressWidgetStrings {
            return when (state) {
                BabyProgressWidgetState.NeedsSetup -> empty(
                    setupMessage = context.getString(R.string.widget_setup_message),
                )

                BabyProgressWidgetState.InvalidFutureLastPeriodDate -> empty(
                    invalidDateMessage = context.getString(R.string.widget_invalid_date),
                )

                is BabyProgressWidgetState.Ongoing -> {
                    val babySizeLabel = weekContent?.babySizeLabel
                        ?: context.getString(R.string.widget_unknown_size)
                    BabyProgressWidgetStrings(
                        setupMessage = "",
                        invalidDateMessage = "",
                        week = context.getString(
                            R.string.widget_week,
                            state.progress.gestationalAge.completedWeeks,
                        ),
                        babySize = context.getString(
                            R.string.widget_baby_size,
                            babySizeLabel,
                        ),
                        babySizeContentDescription = weekContent?.let {
                            context.getString(R.string.widget_baby_size_description, babySizeLabel)
                        },
                        dueDateRelation = dueDateRelationText(
                            context,
                            state.progress.dueDateRelation,
                        ),
                        gestationalAge = "",
                        phaseTitle = "",
                        reviewDateMessage = "",
                    )
                }

                is BabyProgressWidgetState.LateTerm -> phaseStrings(
                    context = context,
                    phaseTitle = context.getString(R.string.widget_late_term),
                    progress = state.progress,
                )

                is BabyProgressWidgetState.PostTerm -> phaseStrings(
                    context = context,
                    phaseTitle = context.getString(R.string.widget_post_term),
                    progress = state.progress,
                )
            }
        }

        private fun empty(
            setupMessage: String = "",
            invalidDateMessage: String = "",
        ) = BabyProgressWidgetStrings(
            setupMessage = setupMessage,
            invalidDateMessage = invalidDateMessage,
            week = "",
            babySize = "",
            babySizeContentDescription = null,
            dueDateRelation = "",
            gestationalAge = "",
            phaseTitle = "",
            reviewDateMessage = "",
        )

        private fun phaseStrings(
            context: Context,
            phaseTitle: String,
            progress: com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetDetails,
        ) = BabyProgressWidgetStrings(
            setupMessage = "",
            invalidDateMessage = "",
            week = "",
            babySize = "",
            babySizeContentDescription = null,
            dueDateRelation = dueDateRelationText(context, progress.dueDateRelation),
            gestationalAge = context.getString(
                R.string.widget_gestational_age,
                progress.gestationalAge.completedWeeks,
                progress.gestationalAge.daysIntoWeek,
            ),
            phaseTitle = phaseTitle,
            reviewDateMessage = context.getString(R.string.widget_review_date),
        )

        private fun dueDateRelationText(
            context: Context,
            relation: DueDateRelation,
        ): String = when (relation) {
            is DueDateRelation.Upcoming -> context.getString(
                R.string.widget_days_until_due_date,
                relation.days,
            )

            DueDateRelation.Today -> context.getString(R.string.widget_due_date_today)
            is DueDateRelation.Elapsed -> context.getString(
                R.string.widget_days_since_due_date,
                relation.days,
            )
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BabyProgressWidgetDependencies {
    fun pregnancyRepository(): PregnancyRepository

    fun pregnancyCalculator(): PregnancyCalculator

    fun pregnancyContentRepository(): PregnancyContentRepository

    fun appLanguageProvider(): AppLanguageProvider

    fun clock(): Clock
}

private val WidgetContent = ColorProvider(R.color.widget_content)
private val WidgetContentMuted = ColorProvider(R.color.widget_content_muted)
private val WidgetRingSurface = ColorProvider(R.color.widget_ring_surface)
private val WidgetProgressTrack = ColorProvider(R.color.widget_progress_track)
private const val WIDGET_RING_SIZE_DP = 100
private const val PHASE_TEXT_WIDTH_DP = 184

private fun com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetDetails
    .completionFraction(): Float {
    return (
        gestationalAge.completedWeeks.toFloat() +
            gestationalAge.daysIntoWeek.toFloat() / PregnancyCalculator.DAYS_PER_WEEK
        ).div(TOTAL_PREGNANCY_WEEKS).coerceIn(0f, 1f)
}

private const val TOTAL_PREGNANCY_WEEKS = 40f

private object BabySizeProgressImageRenderer {
    fun create(
        context: Context,
        @DrawableRes drawableRes: Int,
        progress: Float,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (RING_SIZE_DP * density).toInt()
        val strokeWidth = RING_STROKE_DP * density
        val center = size / 2f
        val outerRadius = center - strokeWidth / 2f
        val fruitBounds = RectF(strokeWidth, strokeWidth, size - strokeWidth, size - strokeWidth)
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.style = Paint.Style.FILL
        paint.color = context.getColor(R.color.widget_ring_surface)
        canvas.drawCircle(center, center, outerRadius, paint)

        val fruitClip = Path().apply {
            addCircle(center, center, fruitBounds.width() / 2f, Path.Direction.CW)
        }
        val source = BitmapFactory.decodeResource(context.resources, drawableRes)
        canvas.withClip(fruitClip) {
            paint.alpha = OPAQUE_ALPHA
            drawBitmap(source, null, fruitBounds, paint)
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = context.getColor(R.color.widget_ring_track)
        canvas.drawCircle(center, center, outerRadius, paint)

        paint.color = context.getColor(R.color.widget_content)
        canvas.drawArc(
            RectF(
                strokeWidth / 2f,
                strokeWidth / 2f,
                size - strokeWidth / 2f,
                size - strokeWidth / 2f,
            ),
            -90f,
            progress.coerceIn(0f, 1f) * 360f,
            false,
            paint,
        )
        return bitmap
    }

    private const val RING_SIZE_DP = WIDGET_RING_SIZE_DP
    private const val RING_STROKE_DP = 5
    private const val OPAQUE_ALPHA = 255
}
