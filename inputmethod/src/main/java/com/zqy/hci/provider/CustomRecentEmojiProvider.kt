package com.zqy.hci.provider

import android.content.Context
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.zqyemoji.emoji2.emojipicker.RecentEmojiAsyncProvider

internal class CustomRecentEmojiProvider(
    context: Context
) : RecentEmojiAsyncProvider {

    companion object {
        private const val PREF_KEY_CUSTOM_EMOJI_FREQ = "pref_key_custom_emoji_freq"
        private const val RECENT_EMOJI_LIST_FILE_NAME =
            "com.zqyemoji.emoji2.emojipicker.sample.preferences"
        private const val SPLIT_CHAR = ","
        private const val KEY_VALUE_DELIMITER = "="
    }

    private val sharedPreferences =
        context.getSharedPreferences(RECENT_EMOJI_LIST_FILE_NAME, Context.MODE_PRIVATE)

    private val emoji2Frequency: MutableMap<String, Int> by lazy {
        sharedPreferences.getString(PREF_KEY_CUSTOM_EMOJI_FREQ, null)?.split(SPLIT_CHAR)
            ?.associate { entry ->
                entry.split(KEY_VALUE_DELIMITER, limit = 2).takeIf { it.size == 2 }
                    ?.let { it[0] to it[1].toInt() } ?: ("" to 0)
            }?.toMutableMap() ?: mutableMapOf()
    }

    override fun getRecentEmojiListAsync(): ListenableFuture<List<String>> =
        Futures.immediateFuture(emoji2Frequency.toList().sortedByDescending { it.second }
            .map { it.first }.reversed().take(11))

    override fun recordSelection(emoji: String) {
        emoji2Frequency[emoji] = (emoji2Frequency[emoji] ?: 0) + 1
        saveToPreferences()
    }

    private fun saveToPreferences() {
        sharedPreferences.edit()
            .putString(PREF_KEY_CUSTOM_EMOJI_FREQ, emoji2Frequency.entries.joinToString(SPLIT_CHAR))
            .apply()
    }
}