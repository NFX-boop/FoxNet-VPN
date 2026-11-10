package com.foxnet.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxnet.app.R
import com.foxnet.app.ui.theme.White

/**
 * The "Info" dialog shown from the bottom bar's Info tab (the former Share dialog): the wide
 * FoxNet logo, the about/attribution blocks with clickable GitHub/Telegram links, and a single
 * "Close" action. Composable and callback names are kept from the original Share dialog so
 * every existing call site keeps working unchanged.
 */
@Composable
fun ShareDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF00C1523),
        tonalElevation = 0.dp,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.foxnet_logo_info),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .aspectRatio(6f),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.foxnet_info_title),
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.foxnet_info_subtitle),
                    color = White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                InfoLinkBlock(
                    label = stringResource(R.string.foxnet_info_developer_label),
                    githubHtml = stringResource(R.string.foxnet_info_developer_github),
                    telegramHtml = stringResource(R.string.foxnet_info_telegram),
                )
                InfoLinkBlock(
                    label = stringResource(R.string.foxnet_info_original_label),
                    githubHtml = stringResource(R.string.foxnet_info_original_source),
                    telegramHtml = null,
                )
                Text(
                    text = stringResource(R.string.foxnet_info_license),
                    color = White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_button_text))
            }
        },
    )
}

@Composable
private fun InfoLinkBlock(label: String, githubHtml: String, telegramHtml: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Text(
            text = label,
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.padding(top = 4.dp, start = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_github_24),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            HtmlLinkText(
                html = githubHtml,
                color = White,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        if (telegramHtml != null) {
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_telegram_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                HtmlLinkText(
                    html = telegramHtml,
                    color = White,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}
