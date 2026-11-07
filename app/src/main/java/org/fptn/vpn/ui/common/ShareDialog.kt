package com.foxnet.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxnet.app.R
import com.foxnet.app.ui.theme.White

/**
 * The "Info" dialog shown from the bottom bar's Info tab (the former Share dialog): the
 * FoxNet logo, the about/attribution text and a single "Close" action. Composable and
 * callback names are kept from the original Share dialog so every existing call site
 * keeps working unchanged.
 */
@Composable
fun ShareDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.foxnet_logo_info),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                )
                HtmlLinkText(
                    html = stringResource(R.string.info_dialog_html),
                    color = White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
