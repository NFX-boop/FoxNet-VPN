package com.foxnet.app.ui.login

import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elvishew.xlog.XLog
import kotlinx.coroutines.launch
import com.foxnet.app.R
import com.foxnet.app.ui.qrscanner.QrScannerScreen
import com.foxnet.app.ui.qrscanner.QrTokenValidator
import com.foxnet.app.ui.qrscanner.ScanQrButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.foxnet.app.ui.common.LegacyPillButton
import com.foxnet.app.ui.common.legacyDrawableBackground
import com.foxnet.app.ui.theme.Black
import com.foxnet.app.ui.theme.Hint
import com.foxnet.app.ui.theme.Primary
import com.foxnet.app.ui.theme.Yellow
import com.foxnet.app.utils.ResourcesUtils.getStringResourceByName
import com.foxnet.app.exception.ErrorCode
import com.foxnet.app.exception.PVNClientException

private const val TAG = "LoginScreen"
private val TOKEN_CLIPBOARD_PREFIXES = listOf("fptn:", "fptnb:")

/**
 * Compose port of the legacy `LoginActivity` / `login_layout.xml`. Reuses
 * [LoginActivityViewModel] unchanged; only the view layer is new.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginActivityViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val errorText by viewModel.errorTextLiveData.observeAsState("")
    var tokenText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var showQrScanner by remember { mutableStateOf(false) }
    var qrImported by rememberSaveable { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // The legacy field never shows the soft keyboard (it's meant to be pasted into), so hide it
    // the moment focus lands here, matching `setShowSoftInputOnFocus(false)`.
    LaunchedEffect(isFocused) {
        if (isFocused) keyboardController?.hide()
    }

    fun onPaste() {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        val text = clipboard?.primaryClip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.text?.toString()?.trim()
        if (text != null && TOKEN_CLIPBOARD_PREFIXES.any { text.startsWith(it) }) {
            qrImported = false
            tokenText = TextFieldValue(text, selection = TextRange(text.length))
        }
    }

    fun onLogin(token: String = tokenText.text, fromQr: Boolean = qrImported) {
        if (submitting) return
        submitting = true
        coroutineScope.launch {
            try {
                if (fromQr) {
                    val status = withContext(Dispatchers.Default) { QrTokenValidator.validate(token) }
                    if (status != QrTokenValidator.Status.VALID) {
                        val message = context.getString(if (status == QrTokenValidator.Status.EMPTY_SERVERS) R.string.qr_empty else R.string.qr_damaged)
                        viewModel.errorTextLiveData.postValue(message)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }
                viewModel.parseAndSaveToken(if (fromQr) QrTokenValidator.normalize(token) else token)
                onLoginSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!fromQr) XLog.tag(TAG).e("Login failed: %s", e.message)
                val localizedMessage = if (fromQr) {
                    context.getString(R.string.qr_import_failed)
                } else if (e is PVNClientException && e.errorCode != ErrorCode.UNKNOWN_ERROR) {
                    getStringResourceByName(context, e.errorCode.value) ?: e.message.orEmpty()
                } else {
                    e.message.orEmpty()
                }
                Toast.makeText(context, localizedMessage, Toast.LENGTH_SHORT).show()
                viewModel.errorTextLiveData.postValue(localizedMessage)
            } finally {
                submitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .legacyDrawableBackground(R.drawable.application_background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.foxnet_logo_info),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .aspectRatio(6f),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp, max = 140.dp)
                .padding(top = 32.dp)
                .legacyDrawableBackground(R.drawable.round_back_white10_20),
        ) {
            BasicTextField(
                value = tokenText,
                onValueChange = { tokenText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 76.dp),
                textStyle = TextStyle(color = Black, fontSize = 16.sp),
                singleLine = false,
                maxLines = 5,
                cursorBrush = SolidColor(Black),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.TopStart) {
                        if (tokenText.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.paste_your_token),
                                color = Hint,
                                fontSize = 16.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_content_paste_24),
                    contentDescription = stringResource(R.string.paste_button),
                    tint = Primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onPaste() },
                )
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_close_24),
                    contentDescription = stringResource(R.string.clear_button),
                    tint = Primary,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(24.dp)
                        .clickable { qrImported = false; tokenText = TextFieldValue("") },
                )
            }
        }

        ScanQrButton(
            onClick = { keyboardController?.hide(); showQrScanner = true },
            enabled = !submitting,
            modifier = Modifier.padding(top = 16.dp),
        )

        LegacyPillButton(
            text = stringResource(R.string.login_button),
            backgroundDrawable = R.drawable.round_back_secondary_100,
            textColor = Primary,
            onClick = { onLogin() },
            enabled = !submitting,
            modifier = Modifier.padding(top = 32.dp),
        )

        Text(
            text = errorText,
            color = Yellow,
            modifier = Modifier.padding(start = 5.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
        )
    }

    if (showQrScanner) {
        QrScannerScreen(
            onDismiss = { showQrScanner = false },
            onToken = { token ->
                showQrScanner = false
                qrImported = true
                tokenText = TextFieldValue(token, selection = TextRange(token.length))
                viewModel.errorTextLiveData.postValue("")
                onLogin(token, fromQr = true)
            },
        )
    }
}
