package com.vikingelectronics.softphone.activity

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

data class ActivityEntry(
    val timestamp: String,
    val snapshot: ImageBitmap,
    val activityDescription: String
)
