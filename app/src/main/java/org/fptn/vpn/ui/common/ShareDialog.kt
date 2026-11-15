package com.foxnet.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxnet.app.R
import com.foxnet.app.ui.theme.White

/**
 * The "Info" dialog shown from the bottom bar. Keeps the existing FoxNet logo and
 * clickable attribution links while separating the left-aligned description from
 * the centered project links.
 */
@Composable
fun ShareDialog(onDismiss: () -> Unit) {
    val maxDialogContentHeight = (LocalConfiguration.current.screenHeightDp * 0.6f).dp

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF00C1523),
        tonalElevation = 0.dp,
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = maxDialogContentHeight)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.foxnet_logo_info),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .aspectRatio(6f),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.foxnet_info_subtitle),
                    color = White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(White.copy(alpha = 0.12f)),
                )
                Spacer(modifier = Modifier.height(15.dp))
                InfoLinkBlock(
                    label = stringResource(R.string.foxnet_info_project_label),
                    githubHtml = stringResource(R.string.foxnet_info_developer_github),
                    telegramHtml = stringResource(R.string.foxnet_info_telegram),
                    sourceHtml = stringResource(R.string.foxnet_info_original_source),
                )
                Text(
                    text = stringResource(R.string.foxnet_info_license),
                    color = White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                ) {
                    Text(stringResource(R.string.close_button_text))
                }
            }
        },
        dismissButton = {},
    )
}

@Composable
private fun InfoLinkBlock(
    label: String,
    githubHtml: String,
    telegramHtml: String,
    sourceHtml: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        InfoLinkRow(icon = R.drawable.ic_github_24, html = githubHtml)
        InfoLinkRow(icon = R.drawable.ic_telegram_24, html = telegramHtml)
        InfoLinkRow(icon = R.drawable.ic_github_24, html = sourceHtml)
    }
}

@Composable
private fun InfoLinkRow(icon: Int, html: String) {
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        HtmlLinkText(
            html = html,
            color = White,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
