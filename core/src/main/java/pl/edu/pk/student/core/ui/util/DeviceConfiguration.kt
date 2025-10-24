package pl.edu.pk.student.core.ui.util

import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    companion object{
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration{
            val widthClass = windowSizeClass.windowWidthSizeClass
            val heightClass = windowSizeClass.windowHeightSizeClass

            return when{
                widthClass == WindowWidthSizeClass.Companion.COMPACT &&
                        heightClass == WindowHeightSizeClass.Companion.MEDIUM -> MOBILE_PORTRAIT
                widthClass == WindowWidthSizeClass.Companion.COMPACT &&
                        heightClass == WindowHeightSizeClass.Companion.EXPANDED -> MOBILE_PORTRAIT
                widthClass == WindowWidthSizeClass.Companion.EXPANDED &&
                        heightClass == WindowHeightSizeClass.Companion.COMPACT -> MOBILE_LANDSCAPE
                widthClass == WindowWidthSizeClass.Companion.MEDIUM &&
                        heightClass == WindowHeightSizeClass.Companion.EXPANDED -> TABLET_PORTRAIT
                widthClass == WindowWidthSizeClass.Companion.EXPANDED &&
                        heightClass == WindowHeightSizeClass.Companion.MEDIUM -> TABLET_LANDSCAPE
                else -> DESKTOP
            }
        }
    }
}