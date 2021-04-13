package com.vikingelectronics.softphone.ui

import android.widget.RadioGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit = {},
    orientation: RadioGroupOrientation = RadioGroupOrientation.Horizontal
) {
    when(orientation) {
        RadioGroupOrientation.Horizontal -> {
            Column(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onSelect
                )
                Text(text = label)
            }
        }
        RadioGroupOrientation.Vertical -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onSelect
                )
                Text(text = label)
            }
        }
    }
}

sealed class RadioGroupOrientation {
    object Horizontal: RadioGroupOrientation()
    object Vertical: RadioGroupOrientation()
}

@Composable
fun <T> RadioGroup(
    modifier: Modifier = Modifier,
    items: Map<T, String>,
    defaultItemSelection: T? = null,
    onItemSelect: (T) -> Unit = {},
    orientation: RadioGroupOrientation = RadioGroupOrientation.Horizontal
) {

    var selectedItem by remember {
        mutableStateOf(defaultItemSelection)
    }

    when(orientation) {
        RadioGroupOrientation.Horizontal -> LazyRow(
            modifier = modifier
        ) {
            items(items.entries.toList()) {
                RadioButtonWithLabel(
                    label = it.value,
                    selected = selectedItem == it.key,
                    onSelect = {
                        selectedItem = it.key
                        onItemSelect(it.key)
                    },
                    orientation = RadioGroupOrientation.Horizontal
                )
            }
        }
        RadioGroupOrientation.Vertical -> LazyColumn(
            modifier = modifier
        ) {
            items(items.entries.toList()) {
//                RadioButtonWithLabel(
//                    label = it.value,
////                    defaultSelected = it.key == selectedItem,
//                    onSelect = { onItemSelect(it.key) },
//                    orientation = RadioGroupOrientation.Vertical
//                )
            }
        }
    }
}
