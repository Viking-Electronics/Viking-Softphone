package com.vikingelectronics.softphone.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import com.vanpra.composematerialdialogs.MaterialDialog

@Composable
inline fun rememberMaterialDialog(
    crossinline onClose: @DisallowComposableCalls () -> Unit = {},
    crossinline content: @Composable (MaterialDialog.() -> Unit) = {},
): MaterialDialog {
    return remember {
        MaterialDialog(
            onCloseRequest = {
                it.hide()
                onClose()
            }
        )
    }.apply {
        build {
            content()
        }
    }
}