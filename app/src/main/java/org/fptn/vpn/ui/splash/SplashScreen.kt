package com.foxnet.app.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elvishew.xlog.XLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.foxnet.app.R
import com.foxnet.app.database.AppDatabase
import com.foxnet.app.services.snichecker.SniCheckerService
import com.foxnet.app.services.snichecker.SniCheckerServiceState

/**
 * Compose replacement for the old `SplashActivity`.
 *
 * Shows the FoxNet logo (the same asset used as the app icon) on the app's charcoal
 * background and, in parallel, resolves where the app should continue
 * (login / home / bypass-methods) from the Room server count and the SNI checker
 * state. The resolved destination is delivered to [onRouteResolved].
 */
@Composable
fun SplashScreen(
    onRouteResolved: (SplashRoute) -> Unit,
) {
    val context = LocalContext.current
    var routed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val serverCount: Int? = withContext(Dispatchers.IO) {
            try {
                AppDatabase.getInstance(context).serverDAO().getCount().get()
            } catch (t: Throwable) {
                XLog.tag("Splash").e("Failed to read server count: %s", t.message)
                null
            }
        }
        val sniActive = SniCheckerService.getStaticState() == SniCheckerServiceState.ACTIVE
        val route = when {
            (serverCount ?: 0) > 0 -> if (sniActive) SplashRoute.BypassMethods else SplashRoute.Home
            else -> SplashRoute.Login
        }
        XLog.tag("Splash").i("Routing to %s [servers=%d, sniActive=%b]", route, serverCount ?: 0, sniActive)
        if (!routed) {
            routed = true
            onRouteResolved(route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D16)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.foxnet_logo_icon),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onRouteResolved = {})
}
