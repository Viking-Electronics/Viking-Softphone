package com.vikingelectronics.softphone.ui

import android.widget.RadioGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun RadioButtonWithLabel(
    modifier: Modifier = Modifier,
    label: String = "",
    defaultSelected: Boolean = false,
    onSelect: () -> Unit = {},
    orientation: RadioGroupOrientation = RadioGroupOrientation.Horizontal
) {

    val selected = remember { mutableStateOf(defaultSelected) }
    val button = RadioButton(
       selected = selected.value,
       onClick = {
           selected.value = !selected.value
           onSelect.invoke()
       }
    )
    val description = Text(text = label)

    when(orientation) {
        RadioGroupOrientation.Horizontal -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center
            ) {
                button
                description
            }
        }
        RadioGroupOrientation.Vertical -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.Center
            ) {
                button
                description
            }
        }
    }
}

sealed class RadioGroupOrientation {
    object Horizontal: RadioGroupOrientation()
    object Vertical: RadioGroupOrientation()
}

@Preview
@Composable
fun RadioGroup(
    modifier: Modifier = Modifier,
    items: List<String> = listOf(),
    defaultItemSelection: String = "",
    onItemSelect: (String) -> Unit = {},
    orientation: RadioGroupOrientation = RadioGroupOrientation.Horizontal
) {

    when(orientation) {
        RadioGroupOrientation.Horizontal -> {

        }
        RadioGroupOrientation.Vertical -> {

        }
    }
}
