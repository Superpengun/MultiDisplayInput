package com.zqy.hci.theme

import android.content.Context
import androidx.annotation.StyleRes

data class UITheme constructor(
    val context: Context,
    val themeName: String,
    @StyleRes val themeResId: Int,
    @StyleRes val iconThemeResId: Int
)