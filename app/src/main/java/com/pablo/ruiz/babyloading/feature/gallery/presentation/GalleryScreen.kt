package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.SocialDistance
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingCard
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingScreenTitle
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyStatusAttentionContainer
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyStatusPositiveContainer
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.TrackingCadence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun GalleryScreen(
    onStartTracking: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTO_SELECTION),
    ) { uris ->
        viewModel.onEvent(GalleryEvent.PhotosSelected(uris.map { it.toString() }))
    }

    GalleryContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onAddPhotos = {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onStartTracking = onStartTracking,
        modifier = modifier,
    )
}

@Composable
private fun GalleryContent(
    uiState: GalleryUiState,
    onEvent: (GalleryEvent) -> Unit,
    onAddPhotos: () -> Unit,
    onStartTracking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = galleryMessage(uiState.message)
    LaunchedEffect(uiState.message) {
        if (uiState.message != null && message != null) {
            snackbarHostState.showSnackbar(message)
            onEvent(GalleryEvent.MessageShown)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                GallerySections(
                    uiState = uiState,
                    onEvent = onEvent,
                    onAddPhotos = onAddPhotos,
                    onStartTracking = onStartTracking,
                )
            }
        }
    }

    uiState.selectedItem?.let { item ->
        GalleryViewerDialog(
            item = item,
            onDelete = { onEvent(GalleryEvent.DeleteRequested(item.id)) },
            onDismiss = { onEvent(GalleryEvent.DialogDismissed) },
        )
    }
    uiState.pendingDeleteItem?.let { item ->
        DeleteGalleryItemDialog(
            item = item,
            onConfirm = { onEvent(GalleryEvent.DeleteConfirmed) },
            onDismiss = { onEvent(GalleryEvent.DialogDismissed) },
        )
    }
}

@Composable
private fun GallerySections(
    uiState: GalleryUiState,
    onEvent: (GalleryEvent) -> Unit,
    onAddPhotos: () -> Unit,
    onStartTracking: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        contentPadding = PaddingValues(
            start = BabyLoadingSpacing.Medium,
            top = BabyLoadingSpacing.Large,
            end = BabyLoadingSpacing.Medium,
            bottom = 112.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            BabyLoadingScreenTitle(
                title = stringResource(R.string.gallery_title),
                subtitle = stringResource(R.string.gallery_subtitle),
            )
        }
        item {
            BellyTrackingSection(
                uiState = uiState,
                onCadenceSelected = { cadence ->
                    onEvent(GalleryEvent.TrackingCadenceSelected(cadence))
                },
                onItemSelected = { id -> onEvent(GalleryEvent.ItemSelected(id)) },
                onDeleteRequested = { id -> onEvent(GalleryEvent.DeleteRequested(id)) },
                onStartTracking = onStartTracking,
            )
        }
        item {
            UltrasoundGallerySection(
                items = uiState.importedItems,
                isImporting = uiState.isImporting,
                onAddPhotos = onAddPhotos,
                onItemSelected = { id -> onEvent(GalleryEvent.ItemSelected(id)) },
                onDeleteRequested = { id -> onEvent(GalleryEvent.DeleteRequested(id)) },
            )
        }
    }
}

@Composable
private fun BellyTrackingSection(
    uiState: GalleryUiState,
    onCadenceSelected: (TrackingCadence) -> Unit,
    onItemSelected: (String) -> Unit,
    onDeleteRequested: (String) -> Unit,
    onStartTracking: () -> Unit,
) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.tracking_entry_title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.tracking_entry_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TrackingStatusBadge(isDue = uiState.isTrackingDue)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TrackingStat(
                title = stringResource(R.string.tracking_total_photos),
                value = uiState.trackingItems.size.toString(),
                modifier = Modifier.weight(1f),
            )
            TrackingStat(
                title = stringResource(R.string.tracking_next_photo),
                value = nextTrackingPhotoLabel(uiState),
                modifier = Modifier.weight(1f),
            )
        }

        uiState.latestTrackingItem?.let { item ->
            LatestTrackingCapture(
                item = item,
                onSelected = { onItemSelected(item.id) },
                modifier = Modifier.padding(top = 18.dp),
            )
        } ?: EmptyTrackingState(modifier = Modifier.padding(top = 18.dp))

        TrackingCadenceSelector(
            selected = uiState.trackingCadence,
            onSelected = onCadenceSelected,
            modifier = Modifier.padding(top = 18.dp),
        )

        Surface(
            onClick = onStartTracking,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 14.dp, horizontal = BabyLoadingSpacing.Medium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = null)
                Text(
                    text = stringResource(R.string.tracking_entry_action),
                    modifier = Modifier.padding(start = BabyLoadingSpacing.Small),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (uiState.trackingItems.isNotEmpty()) {
            Text(
                text = stringResource(R.string.tracking_timeline_title),
                modifier = Modifier
                    .padding(top = 18.dp)
                    .semantics { heading() },
                style = MaterialTheme.typography.labelLarge,
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(
                    items = uiState.trackingItems,
                    key = GalleryItem::id,
                ) { item ->
                    TrackingTimelineItem(
                        item = item,
                        onSelected = { onItemSelected(item.id) },
                        onDelete = { onDeleteRequested(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingStatusBadge(isDue: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isDue) BabyStatusAttentionContainer else BabyStatusPositiveContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = stringResource(
                if (isDue) R.string.tracking_due_now else R.string.tracking_on_track,
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun TrackingStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.White.copy(alpha = 0.75f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun LatestTrackingCapture(
    item: GalleryItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = checkNotNull(LocalConfiguration.current.locales[0])
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrivateGalleryImage(
            filePath = item.privateFilePath,
            requestedSizePx = 480,
            contentDescription = stringResource(
                R.string.gallery_photo_description,
                sourceLabel(item.source),
            ),
            modifier = Modifier
                .width(104.dp)
                .height(136.dp)
                .clip(MaterialTheme.shapes.medium),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.tracking_latest_capture),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = GalleryDateFormatter.format(
                    item.capturedAt,
                    locale,
                    ZoneId.systemDefault(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.pregnancyWeek?.let { week ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.tracking_week, week),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTrackingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.SocialDistance,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        )
        Text(
            text = stringResource(R.string.tracking_empty_title),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.tracking_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TrackingCadenceSelector(
    selected: TrackingCadence,
    onSelected: (TrackingCadence) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.tracking_cadence),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            TrackingCadence.entries.forEachIndexed { index, cadence ->
                SegmentedButton(
                    selected = cadence == selected,
                    onClick = { onSelected(cadence) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = TrackingCadence.entries.size,
                    ),
                    icon = {},
                    label = {
                        Text(stringResource(R.string.tracking_cadence_days, cadence.intervalDays))
                    },
                )
            }
        }
    }
}

@Composable
private fun TrackingTimelineItem(
    item: GalleryItem,
    onSelected: () -> Unit,
    onDelete: () -> Unit,
) {
    val locale = checkNotNull(LocalConfiguration.current.locales[0])
    Column(
        modifier = Modifier.width(148.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onSelected),
        ) {
            PrivateGalleryImage(
                filePath = item.privateFilePath,
                requestedSizePx = 600,
                contentDescription = stringResource(
                    R.string.gallery_photo_description,
                    sourceLabel(item.source),
                ),
                modifier = Modifier.fillMaxSize(),
            )
            GalleryDeleteButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        Text(
            text = GalleryDateFormatter.format(item.capturedAt, locale, ZoneId.systemDefault()),
            style = MaterialTheme.typography.labelLarge,
        )
        item.pregnancyWeek?.let { week ->
            Text(
                text = stringResource(R.string.tracking_week, week),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UltrasoundGallerySection(
    items: List<GalleryItem>,
    isImporting: Boolean,
    onAddPhotos: () -> Unit,
    onItemSelected: (String) -> Unit,
    onDeleteRequested: (String) -> Unit,
) {
    BabyLoadingCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.gallery_ultrasound_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.gallery_ultrasound_subtitle),
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        UltrasoundPhotoGrid(
            items = items,
            isImporting = isImporting,
            onAddPhotos = onAddPhotos,
            onItemSelected = onItemSelected,
            onDeleteRequested = onDeleteRequested,
            modifier = Modifier.padding(top = 18.dp),
        )

        if (items.isEmpty()) {
            EmptyUltrasoundGallery(modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun UltrasoundPhotoGrid(
    items: List<GalleryItem>,
    isImporting: Boolean,
    onAddPhotos: () -> Unit,
    onItemSelected: (String) -> Unit,
    onDeleteRequested: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalConfiguration.current.fontScale
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columnCount = if (fontScale >= ACCESSIBILITY_FONT_SCALE || maxWidth < 340.dp) 1 else 2
        val cells = items.map<GalleryItem, GalleryGridCell>(GalleryGridCell::Photo) +
            GalleryGridCell.AddPhoto
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            cells.chunked(columnCount).forEach { rowCells ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowCells.forEach { cell ->
                        when (cell) {
                            GalleryGridCell.AddPhoto -> AddPhotoTile(
                                isImporting = isImporting,
                                onClick = onAddPhotos,
                                modifier = Modifier.weight(1f),
                            )
                            is GalleryGridCell.Photo -> UltrasoundPhotoTile(
                                item = cell.item,
                                onSelected = { onItemSelected(cell.item.id) },
                                onDelete = { onDeleteRequested(cell.item.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    repeat(columnCount - rowCells.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun UltrasoundPhotoTile(
    item: GalleryItem,
    onSelected: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(180.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onSelected),
    ) {
        PrivateGalleryImage(
            filePath = item.privateFilePath,
            requestedSizePx = 720,
            contentDescription = stringResource(
                R.string.gallery_photo_description,
                sourceLabel(item.source),
            ),
            modifier = Modifier.fillMaxSize(),
        )
        GalleryDeleteButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun AddPhotoTile(
    isImporting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    Column(
        modifier = modifier
            .height(180.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.36f))
            .dashedBorder(borderColor)
            .clickable(enabled = !isImporting, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isImporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = stringResource(R.string.gallery_add_photo),
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun EmptyUltrasoundGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Small),
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        )
        Text(
            text = stringResource(R.string.gallery_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.gallery_empty_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GalleryDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(BabyLoadingSpacing.ExtraSmall)
            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.gallery_delete_photo),
            tint = Color.White,
        )
    }
}

@Composable
private fun PrivateGalleryImage(
    filePath: String,
    requestedSizePx: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = filePath,
        key2 = requestedSizePx,
    ) {
        value = GalleryBitmapLoader.load(filePath, requestedSizePx)
    }
    bitmap?.let { image ->
        Image(
            bitmap = image,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } ?: Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.BrokenImage,
            contentDescription = stringResource(R.string.gallery_image_unavailable),
        )
    }
}

@Composable
private fun GalleryViewerDialog(
    item: GalleryItem,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.gallery_delete_photo),
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.gallery_close_photo),
                        )
                    }
                }
                PrivateGalleryImage(
                    filePath = item.privateFilePath,
                    requestedSizePx = 1600,
                    contentDescription = stringResource(
                        R.string.gallery_photo_description,
                        sourceLabel(item.source),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            if (item.source == GallerySource.GuidedTracking) 9f / 16f else 1f,
                        ),
                )
                Text(
                    text = sourceLabel(item.source),
                    modifier = Modifier.padding(BabyLoadingSpacing.Medium),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun DeleteGalleryItemDialog(
    item: GalleryItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.gallery_delete_title)) },
        text = {
            Text(
                stringResource(
                    if (item.source == GallerySource.GuidedTracking) {
                        R.string.gallery_delete_tracking_message
                    } else {
                        R.string.gallery_delete_imported_message
                    },
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.gallery_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun sourceLabel(source: GallerySource): String {
    return stringResource(
        when (source) {
            GallerySource.Imported -> R.string.gallery_source_imported
            GallerySource.GuidedTracking -> R.string.gallery_source_tracking
        },
    )
}

@Composable
private fun nextTrackingPhotoLabel(uiState: GalleryUiState): String {
    return when {
        uiState.nextTrackingPhotoDate == null -> stringResource(R.string.tracking_start_now)
        uiState.isTrackingDue -> stringResource(R.string.tracking_due_now)
        else -> {
            val locale = checkNotNull(LocalConfiguration.current.locales[0])
            GalleryDateFormatter.format(checkNotNull(uiState.nextTrackingPhotoDate), locale)
        }
    }
}

@Composable
private fun galleryMessage(message: GalleryUserMessage?): String? = when (message) {
    is GalleryUserMessage.ImportCompleted -> pluralStringResource(
        R.plurals.gallery_import_completed,
        message.importedCount,
        message.importedCount,
    )
    is GalleryUserMessage.ImportPartiallyCompleted -> stringResource(
        R.string.gallery_import_partial,
        message.importedCount,
        message.failedCount,
    )
    GalleryUserMessage.ImportFailed -> stringResource(R.string.gallery_import_failed)
    GalleryUserMessage.DeleteFailed -> stringResource(R.string.gallery_delete_failed)
    GalleryUserMessage.CadenceUpdateFailed -> stringResource(R.string.gallery_cadence_update_failed)
    null -> null
}

private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(20.dp.toPx()),
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
        ),
    )
}

private sealed interface GalleryGridCell {
    data class Photo(val item: GalleryItem) : GalleryGridCell

    data object AddPhoto : GalleryGridCell
}

@Preview(showBackground = true)
@Composable
private fun EmptyGalleryPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground {
            GalleryContent(
                uiState = GalleryUiState(isLoading = false),
                onEvent = {},
                onAddPhotos = {},
                onStartTracking = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryWithTrackingPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground {
            GalleryContent(
                uiState = GalleryUiState(
                    isLoading = false,
                    items = listOf(
                        GalleryItem(
                            id = "tracking-preview",
                            privateFilePath = "/preview-tracking.jpg",
                            capturedAt = Instant.parse("2026-08-15T12:00:00Z"),
                            source = GallerySource.GuidedTracking,
                            pregnancyWeek = 24,
                        ),
                        GalleryItem(
                            id = "ultrasound-preview",
                            privateFilePath = "/preview-ultrasound.jpg",
                            capturedAt = Instant.parse("2026-08-10T12:00:00Z"),
                            source = GallerySource.Imported,
                        ),
                    ),
                    nextTrackingPhotoDate = LocalDate.parse("2026-08-22"),
                ),
                onEvent = {},
                onAddPhotos = {},
                onStartTracking = {},
            )
        }
    }
}

private const val MAX_PHOTO_SELECTION = 10
private const val ACCESSIBILITY_FONT_SCALE = 1.3f
