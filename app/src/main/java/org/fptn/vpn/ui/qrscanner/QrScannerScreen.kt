package com.foxnet.app.ui.qrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foxnet.app.R
import com.foxnet.app.ui.theme.Primary
import com.foxnet.app.ui.theme.Secondary
import com.foxnet.app.ui.theme.White
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QrScannerScreen(onToken: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val hostView = LocalView.current
    val deliver by rememberUpdatedState(onToken)
    val dismiss by rememberUpdatedState(onDismiss)
    fun permissionGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var granted by remember { mutableStateOf(permissionGranted()) }
    var busy by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    var delivered by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    var cameraFailed by remember { mutableStateOf(false) }
    var torch by remember { mutableStateOf(false) }
    val hasCamera = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
    val hasFlash = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    LaunchedEffect(Unit) {
        if (hasCamera && !granted) permission.launch(Manifest.permission.CAMERA)
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = permissionGranted()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    fun accept(raw: String) {
        if (busy || delivered || error != null) return
        busy = true
        scope.launch {
            val status = withContext(Dispatchers.Default) { QrTokenValidator.validate(raw) }
            busy = false
            when (status) {
                QrTokenValidator.Status.VALID -> {
                    delivered = true
                    val feedback = if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
                    hostView.performHapticFeedback(feedback)
                    deliver(QrTokenValidator.normalize(raw))
                }
                QrTokenValidator.Status.WRONG_FORMAT -> error = R.string.qr_invalid_message
                QrTokenValidator.Status.EMPTY_SERVERS -> error = R.string.qr_empty
                else -> error = R.string.qr_damaged
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        picking = false
        if (uri != null && !delivered && !busy) {
            busy = true
            scope.launch {
                val result = try {
                    withContext(Dispatchers.IO) { QrImageDecoder.decode(context.contentResolver, uri) }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
                busy = false
                if (result == null) error = R.string.qr_no_code else accept(result)
            }
        }
    }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Primary).safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.qr_title), color = White, fontSize = 20.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { dismiss() }) {
                    Icon(painterResource(R.drawable.ic_baseline_close_24), stringResource(R.string.qr_close), tint = White)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (granted && hasCamera && !cameraFailed && !busy && !picking && error == null && !delivered) {
                    CameraPreviewView(
                        lifecycle = lifecycle,
                        torch = torch,
                        onCode = ::accept,
                        onError = { cameraFailed = true; torch = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (busy || picking) {
                    CircularProgressIndicator(color = Secondary)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(painterResource(R.drawable.ic_qr_scan_24), null, tint = Secondary, modifier = Modifier.size(56.dp))
                        Text(
                            stringResource(if (!hasCamera || cameraFailed) R.string.qr_camera_error else R.string.qr_permission),
                            color = White, textAlign = TextAlign.Center,
                        )
                        if (hasCamera && !granted) {
                            TextButton(onClick = { permission.launch(Manifest.permission.CAMERA) }) {
                                Text(stringResource(R.string.qr_allow_camera), color = Secondary)
                            }
                            TextButton(onClick = {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName)))
                            }) {
                                Text(stringResource(R.string.qr_camera_settings), color = Secondary)
                            }
                        }
                        if (cameraFailed && granted) {
                            TextButton(onClick = { cameraFailed = false }) { Text(stringResource(R.string.qr_retry), color = Secondary) }
                        }
                    }
                }
            }
            Text(stringResource(if (busy) R.string.qr_processing else R.string.qr_hint), color = White, textAlign = TextAlign.Center)
            if (hasFlash && granted && !cameraFailed) {
                TextButton(enabled = !busy && !picking && error == null, onClick = { torch = !torch }) {
                    Text(stringResource(if (torch) R.string.qr_flash_off else R.string.qr_flash_on), color = Secondary)
                }
            }
            TextButton(enabled = !busy && !picking && error == null && !delivered, onClick = {
                picking = true
                torch = false
                try {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (_: Exception) {
                    picking = false
                    error = R.string.qr_no_code
                }
            }) {
                Text(stringResource(R.string.qr_gallery), color = Secondary, fontSize = 16.sp)
            }
        }
        error?.let { message ->
            AlertDialog(
                onDismissRequest = { error = null },
                containerColor = Primary,
                title = { Text(stringResource(R.string.qr_invalid_title), color = White) },
                text = { Text(stringResource(message), color = White) },
                confirmButton = { TextButton(onClick = { error = null }) { Text(stringResource(R.string.qr_retry), color = Secondary) } },
                dismissButton = { TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.cancel_button), color = White) } },
            )
        }
    }
}

@Composable
private fun CameraPreviewView(
    lifecycle: Lifecycle,
    torch: Boolean,
    onCode: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentCode by rememberUpdatedState(onCode)
    val currentError by rememberUpdatedState(onError)
    val view = remember(context) {
        DecoratedBarcodeView(context).apply {
            setStatusText("")
            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        }
    }
    AndroidView(factory = { view }, modifier = modifier)
    DisposableEffect(view, lifecycle) {
        var active = true
        val listener = object : CameraPreview.StateListener {
            override fun previewSized() = Unit
            override fun previewStarted() = Unit
            override fun previewStopped() = Unit
            override fun cameraClosed() = Unit
            override fun cameraError(error: Exception) { if (active) currentError() }
        }
        view.barcodeView.addStateListener(listener)
        view.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) { if (active) currentCode(result.text) }
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) = Unit
        })
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> view.resume()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> view.pause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) view.resume()
        onDispose {
            active = false
            lifecycle.removeObserver(observer)
            view.pause()
        }
    }
    LaunchedEffect(view, torch) {
        if (torch) view.setTorchOn() else view.setTorchOff()
    }
}
