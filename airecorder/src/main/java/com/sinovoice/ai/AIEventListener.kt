package com.sinovoice.ai

interface AIEventListener {
    fun onEvent(event: AIEvent)
}