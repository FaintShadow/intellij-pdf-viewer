package com.firsttimeinforever.intellij.pdf.viewer.application

import com.firsttimeinforever.intellij.pdf.viewer.application.pdfjs.ViewerAdapter
import com.firsttimeinforever.intellij.pdf.viewer.application.pdfjs.types.Math
import kotlinx.browser.window
import org.w3c.dom.events.WheelEvent

class ScrollManager(private val viewer: ViewerAdapter) {
  private var scrollSpeed: Float = 1.0f
  private var dynamicScrolling: Boolean = false
  private var smoothScrolling: Boolean = false
  private var smoothScrollAnimationId: Int? = null

  fun initialize() {
    // Add event listener for wheel events
    window.addEventListener("wheel", { event ->
      handleScrollEvent(event as WheelEvent)
    }, js("{ passive: false }"))
  }

  fun updateSettings(scrollSpeed: Float, dynamicScrolling: Boolean, smoothScrolling: Boolean) {
    this.scrollSpeed = scrollSpeed
    this.dynamicScrolling = dynamicScrolling
    this.smoothScrolling = smoothScrolling
  }

  private fun handleScrollEvent(event: WheelEvent) {
    event.preventDefault()

    // Calculate adjusted delta based on settings
    var adjustedDeltaY = event.deltaY * scrollSpeed

    // Apply dynamic scrolling if enabled
    if (dynamicScrolling) {
      val zoomFactor = viewer.zoomState.value / 100.0
      adjustedDeltaY *= (1.0 / zoomFactor).coerceIn(0.5, 2.0)
    }

    if (smoothScrolling) {
      performSmoothScroll(adjustedDeltaY)
    } else {
      performDirectScroll(adjustedDeltaY)
    }
  }

  private fun performDirectScroll(delta: Double) {
    // Get the current viewer container
    val container = viewer.viewerApp.pdfViewer.container
    container.scrollTop = container.scrollTop + delta
  }

  private fun performSmoothScroll(delta: Double) {
    // Cancel any ongoing smooth scroll
    smoothScrollAnimationId?.let { window.cancelAnimationFrame(it) }

    val container = viewer.viewerApp.pdfViewer.container
    val targetScrollTop = container.scrollTop + delta
    val startScrollTop = container.scrollTop
    val startTime = window.performance.now()
    val duration = 300

    fun animateScroll(currentTime: Double) {
      val elapsed = currentTime - startTime
      val progress = (elapsed / duration).coerceIn(0.0, 1.0)

      // Ease in-out function
      val easedProgress = if (progress < 0.5) {
        2 * progress * progress
      } else {
        Math.subtract(1, Math.pow(-2 * progress + 2, 2) / 2)
      }

      val distance = targetScrollTop - startScrollTop
      val amount = distance * easedProgress
      container.scrollTop = startScrollTop + amount

      if (progress < 1.0) {
        smoothScrollAnimationId = window.requestAnimationFrame { animateScroll(it) }
      }
    }

    smoothScrollAnimationId = window.requestAnimationFrame { animateScroll(it) }
  }
}
