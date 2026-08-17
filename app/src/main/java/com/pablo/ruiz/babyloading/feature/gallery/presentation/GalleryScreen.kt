package com.pablo.ruiz.babyloading.feature.gallery.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingScreenTitle
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GalleryItem
import com.pablo.ruiz.babyloading.feature.gallery.domain.model.GallerySource
import java.time.Instant
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPhotos,
                icon = {
                    if (uiState.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                    }
                },
                text = { Text(stringResource(R.string.gallery_add_photos)) },
                expanded = !uiState.isImporting,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                GalleryGrid(
                    items = uiState.items,
                    onItemSelected = { id -> onEvent(GalleryEvent.ItemSelected(id)) },
                    onDeleteRequested = { id -> onEvent(GalleryEvent.DeleteRequested(id)) },
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
private fun GalleryGrid(
    items: List<GalleryItem>,
    onItemSelected: (String) -> Unit,
    onDeleteRequested: (String) -> Unit,
    onStartTracking: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 148.dp),
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 900.dp),
        contentPadding = PaddingValues(
            start = BabyLoadingSpacing.Medium,
            top = BabyLoadingSpacing.Large,
            end = BabyLoadingSpacing.Medium,
            bottom = 112.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Small),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            BabyLoadingScreenTitle(
                title = stringResource(R.string.gallery_title),
                modifier = Modifier.padding(bottom = BabyLoadingSpacing.Medium),
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            TrackingEntryCard(onStartTracking = onStartTracking)
        }
        if (items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyGallery()
            }
        } else {
            items(
                items = items,
                key = GalleryItem::id,
            ) { item ->
                GalleryGridItem(
                    item = item,
                    onClick = { onItemSelected(item.id) },
                    onDelete = { onDeleteRequested(item.id) },
                )
            }
        }
    }
}

@Composable
private fun TrackingEntryCard(onStartTracking: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = BabyLoadingSpacing.Medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BabyLoadingSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(BabyLoadingSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.tracking_entry_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.tracking_entry_message),
                    modifier = Modifier.padding(top = BabyLoadingSpacing.ExtraSmall),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                ) {
                    Text(stringResource(R.string.tracking_entry_action))
                }
            }
        }
    }
}

@Composable
private fun GalleryGridItem(
    item: GalleryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val locale = checkNotNull(LocalConfiguration.current.locales[0])
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            PrivateGalleryImage(
                filePath = item.privateFilePath,
                requestedSizePx = 640,
                contentDescription = stringResource(
                    R.string.gallery_photo_description,
                    sourceLabel(item.source),
                ),
                modifier = Modifier.fillMaxSize(),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(BabyLoadingSpacing.Small),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            ) {
                Text(
                    text = sourceLabel(item.source),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(BabyLoadingSpacing.ExtraSmall)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.gallery_delete_photo),
                )
            }
        }
        Text(
            text = GalleryDateFormatter.format(item.capturedAt, locale, ZoneId.systemDefault()),
            modifier = Modifier.padding(BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyGallery() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp, horizontal = BabyLoadingSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.gallery_empty_title),
            modifier = Modifier.padding(top = BabyLoadingSpacing.Medium),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.gallery_empty_message),
            modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
            style = MaterialTheme.typography.bodyLarge,
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
                        .aspectRatio(3f / 4f),
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
    null -> null
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
private fun GalleryGridPreview() {
    BabyLoadingTheme {
        BabyLoadingBackground {
            GalleryContent(
                uiState = GalleryUiState(
                    isLoading = false,
                    items = listOf(
                        GalleryItem(
                            id = "preview",
                            privateFilePath = "/preview.jpg",
                            capturedAt = Instant.parse("2026-08-15T12:00:00Z"),
                            source = GallerySource.GuidedTracking,
                            pregnancyWeek = 24,
                        ),
                    ),
                ),
                onEvent = {},
                onAddPhotos = {},
                onStartTracking = {},
            )
        }
    }
}

private const val MAX_PHOTO_SELECTION = 20
