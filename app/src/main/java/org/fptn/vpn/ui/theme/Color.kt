package com.foxnet.app.ui.theme

import androidx.compose.ui.graphics.Color

// Mirrors res/values/color.xml so the Compose UI and the legacy XML views share colors.
// FoxNet palette: deep night background, contrast card surfaces, cyan accent, light text.
val Primary = Color(0xFF0C1523) // @color/primary — deep night background
val Secondary = Color(0xFF31F4EC) // @color/secondary — cyan accent (connected VPN glow)
val White = Color(0xFFE2E8F0) // @color/white — main readable text
val Black = Color(0xFF000000) // @color/black
val Hint = Color(0xFF94A3B8) // @color/hint — muted hints
val Yellow = Color(0xFF38BDF8) // @color/yellow — legacy name kept; now link/status blue
val Gray = Color(0xFF94A3B8) // @color/gray — secondary text
val TealAccent = Color(0xFF38BDF8) // @color/teal_200 — link blue
