package com.sinovoice.ai.nlu

interface NLUEventListener {
    fun onNLUResult(result: NluResult)
}
