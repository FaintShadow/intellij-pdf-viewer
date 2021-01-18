package com.firsttimeinforever.intellij.pdf.viewer.ui.editor.panel.jcef.events

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery

class MessageEventReceiver private constructor(private val browser: JBCefBrowser): Disposable {
    val subscriptions = mutableMapOf<SubscribableEventType, JBCefJSQuery>()

    fun addHandlerWithResponse(event: SubscribableEventType, handler: (String) -> JBCefJSQuery.Response?) {
        check(subscriptions.contains(event))
        subscriptions[event]!!.addHandler(handler)
    }

    fun addHandler(event: SubscribableEventType, handler: (String) -> Unit) {
        addHandlerWithResponse(event) {
            logger.debug("Received event: $event with data: $it")
            handler(it)
            null
        }
    }

    fun injectSubscriptions() {
        logger.debug("Injecting subscriptions for events: ${subscriptions.values}")
        subscriptions.forEach {
            subscribeToEvent(it.key, it.value)
        }
    }

    private fun subscribeToEvent(event: SubscribableEventType, query: JBCefJSQuery) {
        browser.cefBrowser.executeJavaScript("""
            subscribeToMessageEvent('${event.displayName}', (data) => {
                ${query.inject("JSON.stringify(data)")}
            })
        """.trimIndent(), null, 0)
    }

    override fun dispose() {
        subscriptions.values.forEach { it.dispose() }
    }

    companion object {
        private val logger = thisLogger()

        fun fromList(browser: JBCefBrowser, events: List<SubscribableEventType>): MessageEventReceiver {
            val manager = MessageEventReceiver(browser)
            events.forEach {
                manager.subscriptions.put(it, JBCefJSQuery.create(browser))
            }
            return manager
        }
    }
}
