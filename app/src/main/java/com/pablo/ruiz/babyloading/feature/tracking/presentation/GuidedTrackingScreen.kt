package com.pablo.ruiz.babyloading.feature.tracking.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.designsystem.component.BabyLoadingBackground
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingSpacing
import com.pablo.ruiz.babyloading.core.designsystem.theme.BabyLoadingTheme
import com.pablo.ruiz.babyloading.feature.gallery.presentation.GalleryBitmapLoader
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun GuidedTrackingScreen(
    onBack: () -> Unit,
    onCaptureSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuidedTrackingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasCameraPermission = it },
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    GuidedTrackingContent(
        uiState = uiState,
        hasCameraPermission = hasCameraPermission,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onPhotoCaptured = { data ->
            viewModel.onEvent(GuidedTrackingEvent.PhotoCaptured(data))
        },
        onCaptureFailed = { viewModel.onEvent(GuidedTrackingEvent.CaptureFailed) },
        onErrorShown = { viewModel.onEvent(GuidedTrackingEvent.ErrorShown) },
        onBack = onBack,
        onCaptureSaved = onCaptureSaved,
        modifier = modifier,
    )
}

@Composable
private fun GuidedTrackingContent(
    uiState: GuidedTrackingUiState,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onPhotoCaptured: (ByteArray) -> Unit,
    onCaptureFailed: () -> Unit,
    onErrorShown: () -> Unit,
    onBack: () -> Unit,
    onCaptureSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = when (uiState.error) {
        GuidedTrackingError.CaptureFailed -> stringResource(R.string.tracking_capture_failed)
        GuidedTrackingError.SaveFailed -> stringResource(R.string.tracking_save_failed)
        null -> null
    }
    LaunchedEffect(uiState.error) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
    ) { padding ->
        if (hasCameraPermission) {
            TrackingCamera(
                referenceImagePath = uiState.referenceImagePath,
                isSaving = uiState.isSaving,
                onPhotoCaptured = onPhotoCaptured,
                onCaptureFailed = onCaptureFailed,
                onBack = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            CameraPermissionRequired(
                onRequestPermission = onRequestPermission,
                onBack = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }

    uiState.saveOutcome?.let { outcome ->
        CaptureSavedDialog(
            outcome = outcome,
            onDone = onCaptureSaved,
        )
    }
}

@Composable
private fun TrackingCamera(
    referenceImagePath: String?,
    isSaving: Boolean,
    onPhotoCaptured: (ByteArray) -> Unit,
    onCaptureFailed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    var flashEnabled by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraAvailable by remember { mutableStateOf(true) }
    var isShowingReference by remember(referenceImagePath) {
        mutableStateOf(referenceImagePath != null)
    }
    var referenceOpacity by remember { mutableFloatStateOf(DEFAULT_REFERENCE_OPACITY) }
    val referenceBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = referenceImagePath,
    ) {
        value = referenceImagePath?.let { path ->
            GalleryBitmapLoader.load(path, REFERENCE_IMAGE_SIZE_PX)
        }
    }

    DisposableEffect(cameraController, lifecycleOwner, cameraExecutor) {
        runCatching { cameraController.bindToLifecycle(lifecycleOwner) }
            .onFailure { cameraAvailable = false }
        onDispose {
            cameraController.unbind()
            cameraExecutor.shutdown()
        }
    }
    LaunchedEffect(flashEnabled) {
        runCatching {
            cameraController.imageCaptureFlashMode = if (flashEnabled) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }
        }.onSuccess {
            cameraAvailable = true
        }.onFailure {
            cameraAvailable = false
        }
    }

    BoxWithConstraints(modifier = modifier.background(Color.Black)) {
        val viewport = calculateTrackingGuideGeometry(maxWidth.value, maxHeight.value)
        Box(
            modifier = Modifier
                .width(viewport.width.dp)
                .height(viewport.height.dp)
                .align(Alignment.Center),
        ) {
            AndroidView(
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        controller = cameraController
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            if (referenceBitmap != null && isShowingReference) {
                Image(
                    bitmap = checkNotNull(referenceBitmap),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = referenceOpacity,
                )
            }
            TrackingGuideOverlay(modifier = Modifier.fillMaxSize())
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.46f))
                .padding(BabyLoadingSpacing.Small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.tracking_back),
                    tint = Color.White,
                )
            }
            Text(
                text = stringResource(R.string.tracking_title),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = { flashEnabled = !flashEnabled },
                enabled = cameraAvailable,
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                    contentDescription = stringResource(R.string.tracking_toggle_flash),
                    tint = Color.White,
                )
            }
        }

        TrackingReferenceControls(
            hasReferenceImage = referenceBitmap != null,
            isShowingReference = isShowingReference,
            onShowingReferenceChanged = { isShowingReference = it },
            referenceOpacity = referenceOpacity,
            onReferenceOpacityChanged = { referenceOpacity = it },
            cameraAvailable = cameraAvailable,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 124.dp),
        )

        FilledIconButton(
            onClick = {
                isCapturing = true
                capturePhoto(
                    controller = cameraController,
                    executor = cameraExecutor,
                    mainExecutor = context.mainExecutor,
                    cacheDirectory = context.cacheDir,
                    onSuccess = { data ->
                        isCapturing = false
                        onPhotoCaptured(data)
                    },
                    onFailure = {
                        isCapturing = false
                        onCaptureFailed()
                    },
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .size(80.dp)
                .border(4.dp, Color.White, CircleShape),
            enabled = !isCapturing && !isSaving,
        ) {
            if (isCapturing || isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    strokeWidth = 3.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.tracking_take_photo),
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@Composable
private fun TrackingReferenceControls(
    hasReferenceImage: Boolean,
    isShowingReference: Boolean,
    onShowingReferenceChanged: (Boolean) -> Unit,
    referenceOpacity: Float,
    onReferenceOpacityChanged: (Float) -> Unit,
    cameraAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.58f), MaterialTheme.shapes.large)
            .padding(BabyLoadingSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (cameraAvailable) {
                stringResource(R.string.tracking_alignment_hint)
            } else {
                stringResource(R.string.tracking_camera_unavailable)
            },
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (hasReferenceImage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.tracking_reference_toggle),
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
                Switch(
                    checked = isShowingReference,
                    onCheckedChange = onShowingReferenceChanged,
                )
            }
            if (isShowingReference) {
                Text(
                    text = stringResource(R.string.tracking_reference_opacity),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Slider(
                    value = referenceOpacity,
                    onValueChange = onReferenceOpacityChanged,
                    valueRange = 0f..MAX_REFERENCE_OPACITY,
                )
            }
        }
    }
}

@Composable
private fun TrackingGuideOverlay(modifier: Modifier = Modifier) {
    val guideDescription = stringResource(R.string.tracking_guide_description)
    Canvas(
        modifier = modifier.semantics { contentDescription = guideDescription },
    ) {
        val lineColor = Color.White.copy(alpha = 0.25f)
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))
        listOf(1f / 3f, 2f / 3f).forEach { fraction ->
            drawLine(
                color = lineColor,
                start = Offset(size.width * fraction, 0f),
                end = Offset(size.width * fraction, size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = pathEffect,
            )
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height * fraction),
                end = Offset(size.width, size.height * fraction),
                strokeWidth = 1.dp.toPx(),
                pathEffect = pathEffect,
            )
        }
    }
}

private fun capturePhoto(
    controller: LifecycleCameraController,
    executor: ExecutorService,
    mainExecutor: java.util.concurrent.Executor,
    cacheDirectory: File,
    onSuccess: (ByteArray) -> Unit,
    onFailure: () -> Unit,
) {
    val temporaryFile = runCatching {
        File.createTempFile("guided-capture-", ".jpg", cacheDirectory)
    }.getOrElse {
        onFailure()
        return
    }
    val options = ImageCapture.OutputFileOptions.Builder(temporaryFile)
        .build()
    controller.takePicture(
        options,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val data = runCatching { temporaryFile.readBytes() }
                temporaryFile.delete()
                mainExecutor.execute {
                    data.onSuccess(onSuccess).onFailure { onFailure() }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                temporaryFile.delete()
                mainExecutor.execute(onFailure)
            }
        },
    )
}

@Composable
private fun CameraPermissionRequired(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BabyLoadingBackground(modifier = modifier) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(BabyLoadingSpacing.Small),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.tracking_back),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(BabyLoadingSpacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.tracking_permission_title),
                modifier = Modifier.padding(top = BabyLoadingSpacing.Medium),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.tracking_permission_message),
                modifier = Modifier.padding(top = BabyLoadingSpacing.Small),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(BabyLoadingSpacing.Large))
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.tracking_permission_action))
            }
        }
    }
}

@Composable
private fun CaptureSavedDialog(
    outcome: GuidedTrackingSaveOutcome,
    onDone: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.tracking_saved_title)) },
        text = {
            Text(
                stringResource(
                    if (outcome == GuidedTrackingSaveOutcome.PrivateAndPublic) {
                        R.string.tracking_saved_private_and_public
                    } else {
                        R.string.tracking_saved_private_only
                    },
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onDone) {
                Text(stringResource(R.string.tracking_return_to_gallery))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CameraPermissionRequiredPreview() {
    BabyLoadingTheme {
        CameraPermissionRequired(onRequestPermission = {}, onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CaptureSavedDialogPreview() {
    BabyLoadingTheme {
        Surface {
            CaptureSavedDialog(
                outcome = GuidedTrackingSaveOutcome.PrivateAndPublic,
                onDone = {},
            )
        }
    }
}

private const val DEFAULT_REFERENCE_OPACITY = 0.35f
private const val MAX_REFERENCE_OPACITY = 0.7f
private const val REFERENCE_IMAGE_SIZE_PX = 1600
