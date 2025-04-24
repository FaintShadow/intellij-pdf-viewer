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
    // Use JS Math for calculations to avoid type conversion issues
    val speedFactor = scrollSpeed.toDouble()
    var adjustedDeltaY = js("event.deltaY * speedFactor")

    // Apply dynamic scrolling if enabled
    if (dynamicScrolling) {
      val zoomFactor = viewer.zoomState.value / 100.0
      val zoomAdjustment = Math.max(0.5, Math.min(2.0, 1.0 / zoomFactor))
      adjustedDeltaY = js("adjustedDeltaY * zoomAdjustment")
    }

    if (smoothScrolling) {
      performSmoothScroll(adjustedDeltaY)
    } else {
      performDirectScroll(adjustedDeltaY)
    }
  }

  private fun performDirectScroll(delta: dynamic) {
    // Get the current viewer container
    val container = viewer.viewerApp.pdfViewer.container
    container.scrollTop = js("container.scrollTop + delta")
  }

  private fun performSmoothScroll(delta: dynamic) {
    // Cancel any ongoing smooth scroll
    smoothScrollAnimationId?.let { window.cancelAnimationFrame(it) }

    val container = viewer.viewerApp.pdfViewer.container
    val targetScrollTop = js("container.scrollTop + delta")
    val startScrollTop = container.scrollTop
    val startTime = window.performance.now()
    val duration = 300.0

    fun animateScroll(currentTime: Double) {
      val elapsed = currentTime - startTime
      val progress = Math.min(1.0, Math.max(0.0, elapsed / duration))

      // Ease in-out function
      val easedProgress = if (progress < 0.5) {
        js("2 * progress * progress")
      } else {
        // Use JS Math for consistent behavior
        val t = js("-2 * progress + 2")
        val tSquared = js("t * t")
        js("1 - tSquared / 2")
      }

      val distance = js("targetScrollTop - startScrollTop")
      val amount = js("distance * easedProgress")
      container.scrollTop = js("startScrollTop + amount")

      if (progress < 1.0) {
        smoothScrollAnimationId = window.requestAnimationFrame { animateScroll(it) }
      }
    }

    smoothScrollAnimationId = window.requestAnimationFrame { animateScroll(it) }
  }
}
