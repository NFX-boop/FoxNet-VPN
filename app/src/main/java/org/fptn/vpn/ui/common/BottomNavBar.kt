package com.foxnet.app.ui.common

import android.content.res.ColorStateList
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.foxnet.app.R

/**
 * Compose wrapper around the app's real `BottomNavigationView` (Home / Settings / Info),
 * reproducing `CustomBottomNavigationListener`'s navigation behavior. Kept as an `AndroidView`
 * rather than a Compose `NavigationBar`, so the selected/unselected item styling stays
 * pixel-identical instead of being re-derived. The former "Share" tab is now the "Info" tab
 * (same menu item ID and callback, different title/icon) that opens the about dialog.
 *
 * The bar is styled as a compact dark rounded panel: the view background is a rounded shape
 * drawable, icons/labels are tinted soft green when selected and light gray otherwise, and
 * the outer padding floats the panel above the screen edges.
 *
 * [isHomeScreen]/[isSettingsScreen] mirror the legacy `context instanceof HomeActivity` /
 * `context instanceof SettingsActivity` checks that predate the Compose port: every screen
 * under Settings (Logs, PerAppVpnMode, UpdateToken, Backup, BypassMethods,
 * ExperimentalSettings) shows the Settings tab as selected without *being* the Settings
 * screen, so re-tapping "Settings" from one of them must still navigate to the real Settings
 * screen.
 *
 * [settingsEnabled] mirrors `HomeActivity`'s `settingsMenuItem.setEnabled(!activeState)`: Home
 * disables the Settings tab while a VPN connection is active/connecting, so the user can't
 * navigate away mid-connection. [homeEnabled] is the same idea used by BypassMethodsScreen: while
 * SNI auto-select checking is running, both tabs are disabled so the user can't navigate away and
 * lose sight of it (the app also always reopens directly on BypassMethodsScreen while checking is
 * active — see `SplashScreen`'s `sniActive` routing). Every other screen leaves both at the
 * default (always enabled).
 */
@Composable
fun BottomNavBar(
    isHomeScreen: Boolean,
    isSettingsScreen: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateSettings: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    homeEnabled: Boolean = true,
    settingsEnabled: Boolean = true,
) {
    val selectedItemId = if (isHomeScreen) R.id.menuHome else R.id.menuSettings

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        factory = { context ->
            BottomNavigationView(context).apply {
                inflateMenu(R.menu.bottom_nav_bar_menu)
                // Dark rounded panel look instead of the theme's flat bar.
                background = ContextCompat.getDrawable(context, R.drawable.bottom_nav_bar_background)
                elevation = 0f
                val itemTint = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(
                        context.getColor(R.color.bottom_nav_item_selected),
                        context.getColor(R.color.bottom_nav_item_unselected),
                    ),
                )
                itemIconTintList = itemTint
                itemTextColor = itemTint
                // The bar's own background must reach the real bottom edge of the screen
                // (behind the gesture nav bar) rather than floating above it, so consume the
                // navigation-bar inset here as bottom padding instead of letting an ancestor
                // Composable's `safeDrawingPadding` push the whole bar upward and expose the
                // screen background underneath.
                ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                    val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, navBarInset)
                    insets
                }
            }
        },
        update = { view ->
            // `BottomNavigationView.setSelectedItemId`/toggling a menu item's `isEnabled` can
            // synchronously redispatch through whichever listener is *currently* attached —
            // and since this lambda only re-registers fresh listeners at the end, an in-between
            // dispatch would run the *previous* recomposition's listener, closed over its stale
            // enabled/screen flags. That's exactly what fired a real "navigate to Settings" the
            // instant `settingsEnabled` flipped to false (SNI checking started): the old,
            // still-enabled listener ran before being replaced. Detach listeners first, so
            // nothing can fire off a mutation below, then reattach fresh ones with current
            // values once the view's state is settled.
            view.setOnItemSelectedListener(null)
            view.setOnItemReselectedListener(null)

            if (view.selectedItemId != selectedItemId) {
                view.selectedItemId = selectedItemId
            }
            view.menu.findItem(R.id.menuHome)?.isEnabled = homeEnabled
            view.menu.findItem(R.id.menuSettings)?.isEnabled = settingsEnabled

            view.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    selectedItemId -> true
                    R.id.menuHome -> {
                        if (homeEnabled) onNavigateHome()
                        true
                    }
                    R.id.menuSettings -> {
                        if (settingsEnabled) onNavigateSettings()
                        true
                    }
                    R.id.menuShare -> {
                        onShare()
                        false
                    }
                    else -> false
                }
            }
            view.setOnItemReselectedListener { item ->
                when (item.itemId) {
                    R.id.menuSettings -> if (!isSettingsScreen && settingsEnabled) onNavigateSettings()
                    R.id.menuHome -> if (!isHomeScreen && homeEnabled) onNavigateHome()
                }
            }
        },
    )
}
