package com.firsttimeinforever.intellij.pdf.viewer.ui.editor.panel.jcef.events.objects

import kotlinx.serialization.Serializable
import java.awt.Color

@Serializable
class SetThemeColorsDataObject(
    val background: String,
    val foreground: String,
    val icons: String,
    val documentColorInvertIntensity: Int
) {
    companion object {
        private fun transformColorRgba(color: Color): String =
            color.run { "rgba($red, $green, $blue, $alpha)" }

        fun from(background: Color, foreground: Color, icons: Color, invertIntensity: Int) =
            SetThemeColorsDataObject(
                transformColorRgba(background),
                transformColorRgba(foreground),
                transformColorRgba(icons),
                invertIntensity
            )
    }
}
