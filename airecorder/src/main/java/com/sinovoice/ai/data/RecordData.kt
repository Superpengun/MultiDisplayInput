package com.sinovoice.ai.data

data class RecordData(
    val voiceData: ByteArray,
    val voiceTag: VoiceDataType = VoiceDataType.VOICE_DATA
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordData

        if (!voiceData.contentEquals(other.voiceData)) return false

        return true
    }

    override fun hashCode(): Int {
        return voiceData.contentHashCode()
    }
}

enum class VoiceDataType {
    VOICE_DATA, END_VOICE_DATA
}