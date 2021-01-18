package com.firsttimeinforever.intellij.pdf.viewer.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import com.intellij.util.xmlb.annotations.Transient

@State(name = "PdfViewerSettings", storages = [(Storage("pdf_viewer.xml"))])
class PdfViewerSettings: PersistentStateComponent<PdfViewerSettings> {
    var useCustomColors = false
    var customBackgroundColor: Int = defaultBackgroundColor.rgb
    var customForegroundColor: Int = defaultForegroundColor.rgb
    var customIconColor: Int = defaultIconColor.rgb
    var enableDocumentAutoReload = true
    var documentColorsInvertIntensity: Int = defaultDocumentColorsInvertIntensity
    var invertDocumentColors = false

    @Transient
    private val changeListenersHolder = mutableListOf<PdfViewerSettingsListener>()

    val changeListeners
        get() = changeListenersHolder.toList()

    fun addChangeListener(listener: PdfViewerSettingsListener) {
        changeListenersHolder.add(listener)
    }

    fun removeChangeListener(listener: PdfViewerSettingsListener) {
        changeListenersHolder.remove(listener)
    }

    fun callChangeListeners() {
        changeListeners.forEach { it.settingsChanged(this) }
    }

    override fun getState() = this

    override fun loadState(state: PdfViewerSettings) {
        copyBean(state, this)
    }

    companion object {
        val instance: PdfViewerSettings
            get() = getService(PdfViewerSettings::class.java)

        val defaultBackgroundColor
            get() = EditorColorsManager.getInstance().globalScheme.defaultBackground

        val defaultForegroundColor
            get() = EditorColorsManager.getInstance().globalScheme.defaultForeground

        val defaultIconColor
            get() = defaultForegroundColor

        const val defaultDocumentColorsInvertIntensity = 85
    }
}
