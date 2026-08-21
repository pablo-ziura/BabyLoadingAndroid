package com.pablo.ruiz.babyloading.feature.widget.presentation

import android.content.Context
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
import com.pablo.ruiz.babyloading.core.pregnancy.domain.repository.PregnancyRepository
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetState
import com.pablo.ruiz.babyloading.feature.widget.domain.BabyProgressWidgetStateFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.first
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
        val weekContent = (state as? BabyProgressWidgetState.Progress)?.let { progress ->
            runCatching {
                dependencies.pregnancyContentRepository().contentForWeek(
                    week = progress.completedWeeks,
                    language = dependencies.appLanguageProvider().currentLanguage(),
                )
            }.getOrNull()
        }
        val strings = BabyProgressWidgetStrings.create(context, state, weekContent)
        val babySizeProgressImage = (state as? BabyProgressWidgetState.Progress)
            ?.let { progress ->
                weekContent?.let { content ->
                    ImageProvider(
                        BabySizeProgressImageRenderer.create(
                            context = context,
                            drawableRes = content.babySize.drawableResource(),
                            progress = progress.completedFraction,
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
            is BabyProgressWidgetState.Progress -> ProgressContent(
                state = state,
                strings = strings,
                babySizeProgressImage = babySizeProgressImage,
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
private fun ProgressContent(
    state: BabyProgressWidgetState.Progress,
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
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.daysRemaining.toString(),
                    style = TextStyle(
                        color = WidgetContent,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = strings.days,
                    style = TextStyle(
                        color = WidgetContent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            LinearProgressIndicator(
                progress = state.completedFraction,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = WidgetContent,
                backgroundColor = WidgetProgressTrack,
            )
        }
    }
}

private data class BabyProgressWidgetStrings(
    val setupMessage: String,
    val week: String,
    val babySize: String,
    val babySizeContentDescription: String?,
    val days: String,
) {
    companion object {
        fun create(
            context: Context,
            state: BabyProgressWidgetState,
            weekContent: WeekContent?,
        ): BabyProgressWidgetStrings {
            if (state !is BabyProgressWidgetState.Progress) {
                return BabyProgressWidgetStrings(
                    setupMessage = context.getString(R.string.widget_setup_message),
                    week = "",
                    babySize = "",
                    babySizeContentDescription = null,
                    days = "",
                )
            }
            val babySizeLabel = weekContent?.babySizeLabel
                ?: context.getString(R.string.widget_unknown_size)
            return BabyProgressWidgetStrings(
                setupMessage = "",
                week = context.getString(
                    R.string.widget_week,
                    state.completedWeeks,
                ),
                babySize = context.getString(
                    R.string.widget_baby_size,
                    babySizeLabel,
                ),
                babySizeContentDescription = weekContent?.let {
                    context.getString(R.string.widget_baby_size_description, babySizeLabel)
                },
                days = context.getString(R.string.widget_days),
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
